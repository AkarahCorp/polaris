package dev.akarah.cdata.script.expr.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.entity.Entity;

import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;

public record EntityTeleportRelativeAction(
        Expression position
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushSelectedEntity()
                .pushValue(this.position)
                .bytecode(cb -> cb.astore(1))
                .bytecode(cb -> cb.aload(1))
                .getVectorComponent("x")
                .bytecode(cb -> cb.aload(1))
                .getVectorComponent("y")
                .bytecode(cb -> cb.aload(1))
                .getVectorComponent("z")
                .bytecode(cb -> cb.invokevirtual(
                        JIT.ofClass(Entity.class),
                        "teleportRelative",
                        MethodTypeDesc.of(
                                JIT.ofVoid(),
                                List.of(JIT.ofDouble(), JIT.ofDouble(), JIT.ofDouble())
                        )
                ));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }

    public static Optional<List<Pair<String, Type<?>>>> fields() {
        return Optional.of(List.of(
                Pair.of("position", Type.vec3())
        ));
    }
}
