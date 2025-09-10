package dev.akarah.polaris.script.expr.ast.func;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.util.List;

public record JvmFunctionAction(
        ClassDesc declaringClass,
        String name,
        MethodTypeDesc methodTypeDesc,
        List<Expression> parameters,
        Type<?> returnType,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        int idx = 0;
        var lookup = MethodHandles.lookup();
        for(var expr : parameters) {
            ctx.pushValue(expr);
            try {
                if(expr == null) {
                    continue;
                }
                ctx.typecheck(methodTypeDesc.parameterList().get(idx).resolveConstantDesc(lookup));
            } catch (IllegalArgumentException ignored) {

            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            idx++;
        }
        ctx.invokeStatic(
                declaringClass,
                name,
                methodTypeDesc
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return returnType;
    }
}
