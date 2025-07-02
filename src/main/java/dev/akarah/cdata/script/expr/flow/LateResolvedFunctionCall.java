package dev.akarah.cdata.script.expr.flow;

import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class LateResolvedFunctionCall implements Expression {
    String functionName;
    List<Expression> parameters;
    Expression fullyResolved;

    public LateResolvedFunctionCall(String functionName, List<Expression> parameters) {
        this.functionName = functionName;
        this.parameters = parameters;
    }

    @Override
    public void compile(CodegenContext ctx) {
        this.resolve(ctx).compile(ctx);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return this.resolve(ctx).type(ctx);
    }

    public Expression resolve(CodegenContext ctx) {
        if(this.fullyResolved != null) {
            return this.fullyResolved;
        }

        String resolvedName;

        if(this.parameters.isEmpty()) {
            resolvedName = this.functionName;
        } else {
            resolvedName = this.parameters.getFirst().type(ctx).typeName() + "/" + this.functionName;
        }

        var exprClass = ExtBuiltInRegistries.ACTION_TYPE
                .get(LateResolvedFunctionCall.idName(this.functionName))
                .or(() -> ExtBuiltInRegistries.ACTION_TYPE.get(LateResolvedFunctionCall.idName(resolvedName)))
                .orElseThrow()
                .value();

        var constructorArguments = repeatInArray(parameters.size());
        var emptyArguments = toArray(parameters);

        try {
            var constructor = exprClass.getConstructor(constructorArguments);
            return constructor.newInstance((Object[]) emptyArguments);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResourceLocation idName(String name) {
        return ResourceLocation.withDefaultNamespace(name.replace(".", "/"));
    }


    @SuppressWarnings("unchecked")
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
}
