package dev.akarah.cdata.script.value.values.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

public record OffsetVec3Value(
        ValueProvider location,
        ValueProvider shiftBy
) implements ValueProvider {
    public static MapCodec<OffsetVec3Value> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ValueProvider.VEC3_CODEC.fieldOf("location").forGetter(OffsetVec3Value::location),
            ValueProvider.VEC3_CODEC.fieldOf("shift_by").forGetter(OffsetVec3Value::shiftBy)
    ).apply(instance, OffsetVec3Value::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return this.location.asVec3(ctx).add(this.shiftBy.asVec3(ctx));
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
