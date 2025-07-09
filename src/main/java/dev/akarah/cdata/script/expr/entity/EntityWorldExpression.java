package dev.akarah.cdata.script.expr.entity;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record EntityWorldExpression(
        Expression entityExpression
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.entityExpression)
                .invokeStatic(
                        CodegenUtil.ofClass(EntityUtil.class),
                        "entityWorld",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Level.class),
                                List.of(CodegenUtil.ofClass(Entity.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("entity", Type.entity())
                .returns(Type.world())
                .build();
    }
}
