package dev.akarah.cdata.script.expr.ast.func;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.dsl.DslParser;
import dev.akarah.cdata.script.dsl.DslTokenizer;
import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.ast.value.CdExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionStream;
import dev.akarah.cdata.script.type.StructType;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.type.VariableType;
import dev.akarah.cdata.script.type.VoidType;
import dev.akarah.cdata.script.value.*;
import net.minecraft.resources.ResourceLocation;

import java.lang.constant.ClassDesc;
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
        return this.parameters.getFirst().type(ctx);
    }

    @SuppressWarnings("unchecked")
    public Pair<Class<?>, String>[] functionLookupPossibilities(CodegenContext ctx) {
        return new Pair[]{
                Pair.of(this.virtualType(ctx).typeClass(), this.alternateWithVerboseTypeName(ctx)),
                Pair.of(this.virtualType(ctx).typeClass(), this.functionName.replace(".", "__")),
                Pair.of(GlobalNamespace.class, this.functionName.replace(".", "__")),
        };
    }

    public String alternateWithNormalTypeName(CodegenContext ctx) {
        return this.virtualType(ctx).typeName() + "_" + this.functionName.replace(".", "_");
    }

    public String alternateWithVerboseTypeName(CodegenContext ctx) {
        return (this.virtualType(ctx).verboseTypeName() + "__" + this.functionName)
                .replace("[", "$_")
                .replace("]", "_$")
                .replace(",", "_")
                .replace(".", "__");
    }

    public Optional<Expression> resolveStructGetter(CodegenContext ctx) {
        if(this.parameters.isEmpty()) {
            return Optional.empty();
        }
        var ts = ctx.getTypeOf(this.parameters.getFirst()).flatten().toString();
        if(ts.contains("name=akarahnet.profile.member")) {
            System.out.println(ts);
        }
        if(!(ctx.getTypeOf(this.parameters.getFirst()).flatten() instanceof StructType(String name, List<StructType.Field> fields))) {
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

        var typeSet = DslParser.parseExpressionTypeSet(
                DslTokenizer.tokenize(ResourceLocation.fromNamespaceAndPath("minecraft", "method_type_hint"), methodTypeHint.signature())
                        .getOrThrow()
        );
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
                .replace('/', '_')
                .replace(':', '_')
                .replace('.', '_')
                .replace("-", "_");
    }

    public Optional<Expression> resolveFromUserCode(CodegenContext ctx) {
        var functionName = filterNameToMethodName(this.functionName);
        var functionSchema = Resources.actionManager().expressions().get(functionName);
        if(functionSchema == null) {
            functionName = filterNameToMethodName(this.alternateWithNormalTypeName(ctx));
            functionSchema = Resources.actionManager().expressions().get(functionName);
        }

        if(functionSchema != null) {
            var returnType = functionSchema.returnType().flatten() instanceof VoidType ? void.class : RuntimeValue.class;
            var typeParameters = new ArrayList<ClassDesc>();
            for(var parameter : functionSchema.parameters()) {
                typeParameters.add(parameter.getSecond().classDescType());
            }

            Streams.zip(this.parameters.stream(), functionSchema.parameters().stream(), Pair::of)
                    .forEach(pair -> {
                        if(!ctx.getTypeOf(pair.getFirst()).typeEquals(pair.getSecond().getSecond())) {
                            throw new ParsingException(
                                    "Expected type of "
                                            + pair.getSecond().getSecond().verboseTypeName()
                                            + " for parameter "
                                            + pair.getSecond().getFirst()
                                            + ", got "
                                            + ctx.getTypeOf(pair.getFirst()).verboseTypeName(),
                                    pair.getFirst().span()
                            );
                        }
                    });

            return Optional.of(new UserFunctionAction(
                    functionName,
                    MethodTypeDesc.of(
                            CodegenUtil.ofClass(returnType),
                            typeParameters
                    ),
                    this.parameters
            ));
        }
        return Optional.empty();
    }

    public Expression resolve(CodegenContext ctx) {
        var tries = Arrays.stream(this.functionLookupPossibilities(ctx))
                .map(Pair::getSecond)
                .map(x -> x
                        .replaceFirst("\\$_", "[")
                        .replaceFirst("_\\$", "]")
                        .replace("__", "."))
                .map(x -> "fn " + x)
                .toList()
                .toString();
        return this.resolveFromCache()
                .or(() -> this.resolveStructGetter(ctx))
                .or(() -> this.resolveJvmAction(ctx))
                .or(() -> this.resolveFromUserCode(ctx))
                .orElseThrow(() -> new ParsingException(
                        "Can not resolve function `"
                                + this.functionName
                                + "`. Tried possibilities: "
                                + tries
                                + " on parameters " + this.parameters
                                + " of type " + this.parameters.stream().map(ctx::getTypeOf).toList(),
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
