package dev.akarah.cdata.script.expr.player;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record PlayerSendMessageAction(
        Expression entityExpression,
        Expression message
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.entityExpression)
                .typecheck(Entity.class)
                .pushValue(this.message)
                .typecheck(Component.class)
                .constant(0)
                .invokeStatic(
                        CodegenUtil.ofClass(PlayerUtil.class),
                        "sendSystemMessage",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(CodegenUtil.ofClass(Entity.class), CodegenUtil.ofClass(Component.class), CodegenUtil.ofBoolean())
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("entity", Type.entity())
                .required("message", Type.text())
                .build();
    }
}
