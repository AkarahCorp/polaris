package dev.akarah.cdata.script.expr.flow;

import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.env.RuntimeContext;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cod;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    public Optional<Expression> resolveFromCache() {
        return Optional.ofNullable(this.fullyResolved);
    }

    public String alternateName(CodegenContext ctx) {
        if(this.parameters.isEmpty()) {
            return this.functionName;
        } else {
            return this.parameters.getFirst().type(ctx).typeName() + "/" + this.functionName;
        }
    }

    public Optional<Expression> resolveStandardAction(CodegenContext ctx) {
        var alternateName = this.alternateName(ctx);

        var exprClassOpt = ExtBuiltInRegistries.ACTION_TYPE
                .get(CodegenContext.idName(this.functionName))
                .or(() -> ExtBuiltInRegistries.ACTION_TYPE.get(CodegenContext.idName(alternateName)))
                .map(Holder.Reference::value);

        if(exprClassOpt.isPresent()) {
            var exprClass = exprClassOpt.orElseThrow();
            var constructorArguments = repeatInArray(parameters.size());
            var emptyArguments = toArray(parameters);

            try {
                var constructor = exprClass.getConstructor(constructorArguments);
                return Optional.of(constructor.newInstance((Object[]) emptyArguments));
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return Optional.empty();
    }

    public static String filterNameToMethodName(String input) {
        return input
                .replace('/', '_')
                .replace(':', '_')
                .replace('.', '_');
    }

    public Optional<Expression> resolveFromUserCode(CodegenContext ctx) {
        System.out.println("trying " + this.functionName + " or " + this.alternateName(ctx));


        System.out.println(ExtReloadableResources.actionManager().expressions().keySet());
        var functionName = filterNameToMethodName(this.functionName);
        var functionSchema = ExtReloadableResources.actionManager().expressions().get(functionName);
        if(functionSchema == null) {
            functionName = filterNameToMethodName(this.alternateName(ctx));
            functionSchema = ExtReloadableResources.actionManager().expressions().get(functionName);
        }

        if(functionSchema != null) {
            var returnType = functionSchema.returnType().classDescType();
            var typeParameters = new ArrayList<ClassDesc>();
            typeParameters.add(JIT.ofClass(RuntimeContext.class));
            for(var parameter : functionSchema.parameters()) {
                typeParameters.add(parameter.getSecond().classDescType());
            }

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
                .orElseThrow(() -> new RuntimeException("no clue how to resolve " + this.functionName + "(" + filterNameToMethodName(this.functionName) + ")" + " sorry"));
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
