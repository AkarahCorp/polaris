package dev.akarah.polaris.script.expr.ast.func;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.dsl.DslParser;
import dev.akarah.polaris.script.dsl.DslTokenizer;
import dev.akarah.polaris.script.exception.ParsingException;
import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.expr.ast.value.CdExpression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.params.ExpressionStream;
import dev.akarah.polaris.script.params.ExpressionTypeSet;
import dev.akarah.polaris.script.type.StructType;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.type.VoidType;
import dev.akarah.polaris.script.value.*;
import net.minecraft.resources.ResourceLocation;

import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Method;
import java.util.*;

public class LateResolvedFunctionCall implements Expression {
    String functionName;
    List<Expression> parameters;
    Expression fullyResolved;
    SpanData spanData;

    String declaringClass;
    String jvmFunctionName;
    MethodTypeDesc jvmFunctionType;
    Type<?> cachedReturnType;

    public LateResolvedFunctionCall(String functionName, List<Expression> parameters, SpanData spanData) {
        this.functionName = functionName;
        this.parameters = parameters;
        this.spanData = spanData;
    }

    @Override
    public void compile(CodegenContext ctx) {
        this.resolve(ctx).compile(ctx);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return this.resolve(ctx).type(ctx);
    }

    public Optional<Expression> resolveFromCache() {
        return Optional.ofNullable(this.fullyResolved);
    }

    public Type<?> virtualType(CodegenContext ctx) {
        if(this.parameters.isEmpty()) {
            return Type.any();
        }
        System.out.println("ct: " + this.parameters.getFirst().type(ctx));
        return this.parameters.getFirst().type(ctx);
    }

    @SuppressWarnings("unchecked")
    public Pair<Class<?>, String>[] functionLookupPossibilities(CodegenContext ctx) {
        return new Pair[]{
                Pair.of(this.virtualType(ctx).typeClass(), this.functionName.replace(".", "__")),
                Pair.of(GlobalNamespace.class, this.functionName.replace(".", "__")),
        };
    }

    public String alternateWithNormalTypeName(CodegenContext ctx) {
        var virtualType = this.virtualType(ctx).typeName();
        System.out.println("Vtype: " + virtualType);
        if(virtualType.contains(":")) {
            return virtualType + "/" + this.functionName.replace(".", "/");
        } else {
            return virtualType + ":" + this.functionName;
        }
    }

    public Optional<Expression> resolveStructGetter(CodegenContext ctx) {
        if(this.parameters.isEmpty()) {
            return Optional.empty();
        }
        if(!(ctx.getTypeOf(this.parameters.getFirst()).flatten() instanceof StructType(ResourceLocation name, List<StructType.Field> fields))) {
            return Optional.empty();
        }
        for(var field : fields) {
            if(this.functionName.equals(field.name()) && this.parameters.size() == 2) {
                if(!(ctx.getTypeOf(this.parameters.get(1)).flatten().typeEquals(field.type()))) {
                    throw new ParsingException(
                            "Expected type `" + field.type() + "`, got type `" + ctx.getTypeOf(this.parameters.get(1)) + "`",
                            this.parameters.get(1).span()
                    );
                }
                return Optional.of(new JvmFunctionAction(
                        CodegenUtil.ofClass(RStruct.class),
                        "put",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(
                                        CodegenUtil.ofClass(RStruct.class),
                                        CodegenUtil.ofClass(String.class),
                                        CodegenUtil.ofClass(RuntimeValue.class)
                                )
                        ),
                        List.of(this.parameters.getFirst(), new CdExpression(field.name()), this.parameters.get(1)),
                        Type.void_()
                ));
            }

            if(this.functionName.equals(field.name()) && this.parameters.size() == 1) {
                var fb = field.fallback();
                if(fb == null) {
                    fb = new CdExpression(null);
                }
                return Optional.of(new JvmFunctionAction(
                        CodegenUtil.ofClass(RStruct.class),
                        "get",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(RuntimeValue.class),
                                List.of(
                                        CodegenUtil.ofClass(RStruct.class),
                                        CodegenUtil.ofClass(String.class),
                                        CodegenUtil.ofClass(RuntimeValue.class)
                                )
                        ),
                        List.of(this.parameters.getFirst(), new CdExpression(field.name()), fb),
                        field.type()
                ));
            }
        }
        return Optional.empty();
    }

    static Map<String, ExpressionTypeSet> TYPE_SET_CACHE = Maps.newHashMap();

    public Optional<Expression> resolveJvmAction(CodegenContext ctx) {
        Method method = null;
        for(var pair : this.functionLookupPossibilities(ctx)) {
            if(method != null) {
                continue;
            }
            method = Arrays.stream(pair.getFirst().getDeclaredMethods())
                    .filter(x -> x.getName().equals(pair.getSecond()))
                    .findFirst().orElse(null);
        }

        if(method == null) {
            return Optional.empty();
        }

        if(method.getAnnotations().length == 0) {
            System.out.println("Call " + method.getDeclaringClass() + "#" + this.functionName + " is valid, but lacks an annotation.");
            return Optional.empty();
        }
        if(!(method.getAnnotations()[0] instanceof MethodTypeHint methodTypeHint)) {
            System.out.println("Call " + method.getDeclaringClass() + "#" + this.functionName + " is valid, but lacks a MethodTypeHint annotation in first position.");
            return Optional.empty();
        }


        ExpressionTypeSet typeSet = null;

        if(TYPE_SET_CACHE.containsKey(methodTypeHint.signature())) {
            typeSet = TYPE_SET_CACHE.get(methodTypeHint.signature());
        } else {
            typeSet = DslParser.parseExpressionTypeSet(
                    DslTokenizer.tokenize(ResourceLocation.fromNamespaceAndPath("minecraft", "method_type_hint"), methodTypeHint.signature())
                            .getOrThrow(),
                    method.getName()
            );
            TYPE_SET_CACHE.put(methodTypeHint.signature(), typeSet);
        }

        var newParameters = typeSet.typecheck(ctx, ExpressionStream.of(this.parameters, this.spanData));

        var rt = typeSet.returns();
        for(int i = 0; i < 10; i++) {
            rt = rt.fixTypeVariables(typeSet);
            if(rt == null) {
                throw new ParsingException("Unable to infer return type, something went wrong", this.spanData);
            }
        }

        return Optional.of(new JvmFunctionAction(
                CodegenUtil.ofClass(method.getDeclaringClass()),
                method.getName(),
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(method.getReturnType()),
                        Arrays.stream(method.getParameterTypes())
                                .map(CodegenUtil::ofClass)
                                .toList()
                ),
                newParameters,
                typeSet.returns().fixTypeVariables(typeSet)
        ));
    }


    public static String filterNameToMethodName(String input) {
        return input
                .replaceFirst("\\.", ":")
                .replace("\\.", "/");
    }

    public Optional<Expression> resolveFromUserCode(CodegenContext ctx) {
        System.out.println("fn: " + this.functionName);
        var functionName = filterNameToMethodName(this.functionName);
        var functionSchema = Resources.actionManager().expressions().get(ResourceLocation.parse(functionName));
        if(functionSchema == null) {
            functionName = filterNameToMethodName(this.alternateWithNormalTypeName(ctx));
            functionSchema = Resources.actionManager().expressions().get(ResourceLocation.parse(functionName));
        }

        if(functionSchema != null) {
            var typeSet = functionSchema.typeSet();
            var newParameters = typeSet.typecheck(ctx, ExpressionStream.of(this.parameters, this.spanData));

            var rt = typeSet.returns();
            for(int i = 0; i < 10; i++) {
                rt = rt.fixTypeVariables(typeSet);
                if(rt == null) {
                    throw new ParsingException("Unable to infer return type, something went wrong", this.spanData);
                }
            }

            return Optional.of(new UserFunctionAction(
                    ResourceLocation.parse(functionName),
                    MethodTypeDesc.of(
                            CodegenUtil.ofClass(rt.flatten() instanceof VoidType ? void.class : RuntimeValue.class),
                            typeSet.parameters().stream()
                                    .map(x -> x.typePattern().typeClass())
                                    .map(CodegenUtil::ofClass)
                                    .toList()
                    ),
                    this.parameters
            ));
        }
        return Optional.empty();
    }

    public Expression resolve(CodegenContext ctx) {
        return this.resolveFromCache()
                .or(() -> this.resolveStructGetter(ctx))
                .or(() -> this.resolveJvmAction(ctx))
                .or(() -> this.resolveFromUserCode(ctx))
                .orElseThrow(() -> new ParsingException(
                        "Can not resolve function `"
                                + this.functionName
                                + "`",
                        this.span()
                ));
    }

    @Override
    public SpanData span() {
        return this.spanData;
    }

    @Override
    public String toString() {
        return "LRFC[name=" + this.functionName + ",parameters=" + this.parameters + "]";
    }
}
