package dev.akarah.cdata.script.expr.player;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.classfile.CodeBuilder;
import java.util.List;

public record PlayerSendMessageAction(
        Expression message
) implements Expression {
    public static MapCodec<PlayerSendMessageAction> GENERATOR_CODEC = Expression.codecByType(Type.text())
            .fieldOf("message")
            .xmap(PlayerSendMessageAction::new, PlayerSendMessageAction::message);

    @Override
    public void compile(CodegenContext ctx) {
        ctx.ifSelectionIsType(
                JIT.ofClass(ServerPlayer.class),
                () -> ctx.pushSelectedEntityAs(JIT.ofClass(ServerPlayer.class))
                        .evaluateParsedTextOrNull(this.message)
                        .runIfNonNull(
                                () -> ctx.invokeFromSelection(
                                        JIT.ofClass(ServerPlayer.class),
                                        "sendSystemMessage",
                                        JIT.ofVoid(),
                                        List.of(JIT.ofClass(Component.class))
                                ),
                                () -> ctx.bytecode(CodeBuilder::pop)
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
