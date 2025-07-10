package dev.akarah.cdata.script.expr.entity;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record EntitySetNameAction(
        Expression entityExpression,
        Expression name
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.entityExpression)
                .typecheck(Entity.class)
                .pushValue(name)
                .typecheck(Component.class)
                .invokeStatic(
                        CodegenUtil.ofClass(EntityUtil.class),
                        "setEntityName",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(CodegenUtil.ofClass(Entity.class), CodegenUtil.ofClass(Component.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("entity", Type.entity())
                .required("name", Type.text())
                .build();
    }
}
