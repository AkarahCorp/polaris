package dev.akarah.cdata.script.expr.flow;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.exception.TypeCheckException;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.SpannedExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.params.ExpressionStream;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.core.Holder;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LateResolvedFunctionCall implements Expression {
    String functionName;
    List<Expression> parameters;
    Expression fullyResolved;
    SpanData spanData;

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

    public String alternateWithNormalTypeName(CodegenContext ctx) {
        if(this.parameters.isEmpty()) {
            return this.functionName;
        } else {
            return this.parameters.getFirst().type(ctx).typeName() + "/" + this.functionName;
        }
    }

    public String alternateWithVerboseTypeName(CodegenContext ctx) {
        if(this.parameters.isEmpty()) {
            return this.functionName;
        } else {
            return (this.parameters.getFirst().type(ctx).verboseTypeName() + "/" + this.functionName)
                    .replace("[", "-")
                    .replace("]", "-")
                    .replace(",", "-");
        }
    }

    public Optional<Expression> resolveStandardAction(CodegenContext ctx) {
        var exprClassOpt = ExtBuiltInRegistries.ACTION_TYPE
                .get(CodegenContext.idName(this.functionName))
                .or(() -> ExtBuiltInRegistries.ACTION_TYPE.get(CodegenContext.idName(
                        this.alternateWithNormalTypeName(ctx)
                )))
                .or(() -> ExtBuiltInRegistries.ACTION_TYPE.get(CodegenContext.idName(
                        this.alternateWithVerboseTypeName(ctx)
                )))
                .map(Holder.Reference::value);

        if(exprClassOpt.isPresent()) {
            var exprClass = exprClassOpt.orElseThrow();

            MethodHandles.Lookup lookup = null;
            try {
                lookup = MethodHandles.privateLookupIn(exprClass, MethodHandles.lookup());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            List<Expression> expressionList;
            try {
                var parameterSetCtor = lookup.findStatic(exprClass, "parameters", MethodType.methodType(ExpressionTypeSet.class));
                var parameterSet = (ExpressionTypeSet) parameterSetCtor.invoke();
                expressionList = parameterSet.typecheck(ctx, ExpressionStream.of(this.parameters, this.span()));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }


            var emptyArguments = toArray(expressionList);
            var constructorArguments = repeatInArray(expressionList.size());

            try {
                var constructor = lookup.findConstructor(exprClass, MethodType.methodType(void.class, constructorArguments));
                return Optional.of(new SpannedExpression<>(
                        (Expression) constructor.invokeWithArguments((Object[]) emptyArguments),
                        this.span()
                ));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return Optional.empty();
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
        var functionSchema = ExtReloadableResources.actionManager().expressions().get(functionName);
        if(functionSchema == null) {
            functionName = filterNameToMethodName(this.alternateWithNormalTypeName(ctx));
            functionSchema = ExtReloadableResources.actionManager().expressions().get(functionName);
        }

        if(functionSchema != null) {
            var returnType = functionSchema.returnType().classDescType();
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
                            returnType,
                            typeParameters
                    ),
                    this.parameters
            ));
        }
        return Optional.empty();
    }

    public Expression resolve(CodegenContext ctx) {
        return this.resolveFromCache()
                .or(() -> this.resolveStandardAction(ctx))
                .or(() -> this.resolveFromUserCode(ctx))
                .orElseThrow(() -> new ParsingException("no clue how to resolve `" + this.functionName + "` sorry", this.span()));
    }

    private static Expression[] toArray(List<Expression> list) {
        var array = new Expression[list.size()];
        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    private static Class<Expression>[] repeatInArray(int size) {
        var array = new Class[size];
        Arrays.fill(array, Expression.class);
        return array;
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
