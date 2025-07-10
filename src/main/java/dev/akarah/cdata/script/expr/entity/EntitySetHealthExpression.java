package dev.akarah.cdata.script.expr.entity;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record EntitySetHealthExpression(
        Expression entity,
        Expression amount
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.entity)
                .typecheck(Entity.class)
                .pushValue(this.amount)
                .typecheck(Double.class)
                .invokeStatic(
                        CodegenUtil.ofClass(EntityUtil.class),
                        "setHealth",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(
                                        CodegenUtil.ofClass(Entity.class),
                                        CodegenUtil.ofClass(Double.class)
                                )
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("entity", Type.entity())
                .required("amount", Type.number())
                .returns(Type.void_())
                .build();
    }
}
