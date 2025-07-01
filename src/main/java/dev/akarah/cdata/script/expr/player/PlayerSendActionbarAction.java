package dev.akarah.cdata.script.expr.player;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record PlayerSendActionbarAction(
        Expression message
) implements Expression {
    public static MapCodec<PlayerSendActionbarAction> GENERATOR_CODEC = Expression.codecByType(Type.text())
            .fieldOf("message")
            .xmap(PlayerSendActionbarAction::new, PlayerSendActionbarAction::message);

    @Override
    public void compile(CodegenContext ctx) {
        ctx.ifSelectionIsType(
                JIT.ofClass(ServerPlayer.class),
                ctx2 -> ctx2
                        .pushSelectedEntityAs(JIT.ofClass(ServerPlayer.class))
                        .evaluateParsedTextOrReturn(this.message)
                        .bytecode(cb -> cb.loadConstant(1))
                        .invokeFromSelection(
                                JIT.ofClass(ServerPlayer.class),
                                "sendSystemMessage",
                                JIT.ofVoid(),
                                List.of(JIT.ofClass(Component.class), JIT.ofBoolean())
                        )
        );
    }

    @Override
    public Type<?> type() {
        return Type.void_();
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
