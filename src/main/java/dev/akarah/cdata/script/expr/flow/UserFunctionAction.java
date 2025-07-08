package dev.akarah.cdata.script.expr.flow;

import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cod;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record UserFunctionAction(
        String name,
        MethodTypeDesc methodTypeDesc,
        List<Expression> parameters
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        for(var expr : parameters) {
            ctx.pushValue(expr);
        }
        ctx.invokeStatic(
                CodegenContext.ACTION_CLASS_DESC,
                name,
                methodTypeDesc
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return ExtReloadableResources.actionManager().expressions().get(name).type(ctx);
    }
}
