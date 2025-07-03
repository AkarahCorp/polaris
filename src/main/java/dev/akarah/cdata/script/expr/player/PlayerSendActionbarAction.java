package dev.akarah.cdata.script.expr.player;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.classfile.CodeBuilder;
import java.util.List;
import java.util.Optional;

public record PlayerSendActionbarAction(
        Expression message
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.ifSelectionIsType(
                JIT.ofClass(ServerPlayer.class),
                () -> ctx
                        .pushSelectedEntityAs(JIT.ofClass(ServerPlayer.class))
                        .evaluateParsedTextOrNull(this.message)
                        .bytecode(cb -> cb.loadConstant(1))
                        .runIfNonNull(
                                () -> ctx.invokeFromSelection(
                                    JIT.ofClass(ServerPlayer.class),
                                    "sendSystemMessage",
                                    JIT.ofVoid(),
                                    List.of(JIT.ofClass(Component.class), JIT.ofBoolean())
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
                Pair.of("message", Type.string())
        );
    }
}
