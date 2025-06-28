package dev.akarah.cdata.script.value.values.vec3;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record EntityVec3Value() implements ValueProvider {
    public static MapCodec<EntityVec3Value> GENERATOR_CODEC = MapCodec.unit(EntityVec3Value::new);

    @Override
    public Object evaluate(ScriptContext ctx) {
        return ctx.defaultSelection().accessDefaultEntityOrElse(
                Entity::position,
                () -> Vec3.ZERO
        );
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
