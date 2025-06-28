package dev.akarah.cdata.script.value.values.constants;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

public record NumberValue(double value) implements ValueProvider {
    public static MapCodec<NumberValue> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.fieldOf("item").forGetter(NumberValue::value)
    ).apply(instance, NumberValue::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return this.value();
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
