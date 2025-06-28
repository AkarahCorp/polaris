package dev.akarah.cdata.script.value.values.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

public record MultiplyVec3Value(
        ValueProvider location,
        ValueProvider multiplyBy
) implements ValueProvider {
    public static MapCodec<MultiplyVec3Value> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ValueProvider.VEC3_CODEC.fieldOf("location").forGetter(MultiplyVec3Value::location),
            ValueProvider.VEC3_CODEC.fieldOf("multiply_by").forGetter(MultiplyVec3Value::multiplyBy)
    ).apply(instance, MultiplyVec3Value::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return this.location.asVec3(ctx).multiply(this.multiplyBy.asVec3(ctx));
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
