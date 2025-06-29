package dev.akarah.cdata.script.value.values.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

public record Vec3ZValue(
        ValueProvider vec3
) implements ValueProvider {
    public static MapCodec<Vec3ZValue> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ValueProvider.VEC3_CODEC.fieldOf("variableName").forGetter(Vec3ZValue::vec3)
    ).apply(instance, Vec3ZValue::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return vec3.asVec3(ctx).z;
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
