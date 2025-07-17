package dev.akarah.cdata.script.expr.ast.func;

import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.util.List;

public record UserFunctionAction(
        String name,
        MethodTypeDesc methodTypeDesc,
        List<Expression> parameters
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        int i = 0;
        var lookup = MethodHandles.lookup();
        for(var expr : parameters) {
            try {
                ctx.pushValue(expr).typecheck(methodTypeDesc.parameterList().get(i).resolveConstantDesc(lookup));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            i++;
        }
        ctx.invokeStatic(
                CodegenContext.ACTION_CLASS_DESC,
                name,
                methodTypeDesc
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Resources.actionManager().expressions().get(name).type(ctx);
    }
}
