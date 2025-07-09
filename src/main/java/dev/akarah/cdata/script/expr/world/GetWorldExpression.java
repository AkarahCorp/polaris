package dev.akarah.cdata.script.expr.world;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record GetWorldExpression(
        Expression worldName
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.worldName)
                .invokeStatic(
                        CodegenUtil.ofClass(WorldUtil.class),
                        "getLevel",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Level.class),
                                List.of(CodegenUtil.ofClass(ResourceLocation.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("id", Type.identifier())
                .returns(Type.world())
                .build();
    }
}
