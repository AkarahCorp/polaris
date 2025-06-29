package dev.akarah.cdata.script.value.values.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;
import net.minecraft.world.phys.Vec3;

public record Vec3XValue(
        ValueProvider vec3
) implements ValueProvider {
    public static MapCodec<Vec3XValue> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ValueProvider.VEC3_CODEC.fieldOf("variableName").forGetter(Vec3XValue::vec3)
    ).apply(instance, Vec3XValue::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return vec3.asVec3(ctx).x;
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
