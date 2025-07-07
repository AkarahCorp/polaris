package dev.akarah.cdata.script.expr.player;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.classfile.CodeBuilder;
import java.util.List;

public record PlayerSendActionbarAction(
        Expression entityExpression,
        Expression message
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.entityExpression)
                .bytecode(cb -> cb.dup().instanceOf(CodegenUtil.ofClass(ServerPlayer.class)))
                .ifThen(() -> ctx
                        .bytecode(cb -> cb.checkcast(CodegenUtil.ofClass(ServerPlayer.class)))
                        .runIfNonNull(
                                () -> ctx.pushValue(this.message).invokeFromSelection(
                                    CodegenUtil.ofClass(ServerPlayer.class),
                                    "sendSystemMessage",
                                    CodegenUtil.ofVoid(),
                                    List.of(CodegenUtil.ofClass(Component.class), CodegenUtil.ofBoolean())
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
                Pair.of("message", Type.text())
        );
    }
}
