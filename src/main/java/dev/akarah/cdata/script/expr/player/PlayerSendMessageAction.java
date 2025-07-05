package dev.akarah.cdata.script.expr.player;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.classfile.CodeBuilder;
import java.util.List;

public record PlayerSendMessageAction(
        Expression entityExpression,
        Expression message
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.entityExpression)
                .bytecode(cb -> cb.dup().instanceOf(JIT.ofClass(ServerPlayer.class)))
                .ifThen(() -> ctx
                        .bytecode(cb -> cb.checkcast(JIT.ofClass(ServerPlayer.class)))
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
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("entity", Type.entity()),
                Pair.of("message", Type.string())
        );
    }
}
