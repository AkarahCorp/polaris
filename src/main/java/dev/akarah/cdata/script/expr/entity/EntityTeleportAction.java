package dev.akarah.cdata.script.expr.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.entity.Entity;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record EntityTeleportAction(
        Expression position
) implements Expression {
    public static MapCodec<EntityTeleportAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Expression.codecByType(Type.vec3()).fieldOf("position").forGetter(EntityTeleportAction::position)
    ).apply(instance, EntityTeleportAction::new));

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
                        "teleportTo",
                        MethodTypeDesc.of(
                                JIT.ofVoid(),
                                List.of(JIT.ofDouble(), JIT.ofDouble(), JIT.ofDouble())
                        )
                ));
    }

    @Override
    public Type<?> type() {
        return Type.void_();
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }

    @Override
    public int localsRequiredForCompile() {
        return 1;
    }
}
