package dev.akarah.cdata.script.expr.test;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record PlayerSendMessageAction(
        Expression message
) implements Expression {
    public static MapCodec<PlayerSendMessageAction> GENERATOR_CODEC = Expression.CODEC
            .fieldOf("message")
            .xmap(PlayerSendMessageAction::new, PlayerSendMessageAction::message);

    @Override
    public void compile(CodegenContext ctx) {
        ctx.ifSelectionIsType(
                JIT.ofClass(ServerPlayer.class),
                ctx2 -> ctx2.pushSelectedEntityAs(JIT.ofClass(ServerPlayer.class))
                        .pushValue(this.message)
                        .invokeFromSelection(
                                JIT.ofClass(ServerPlayer.class),
                                "sendSystemMessage",
                                JIT.ofVoid(),
                                List.of(JIT.ofClass(Component.class))
                        )
        );
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
