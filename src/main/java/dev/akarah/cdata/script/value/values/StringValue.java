package dev.akarah.cdata.script.value.values;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

public record StringValue(String value) implements ValueProvider {
    public static MapCodec<StringValue> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("value").forGetter(StringValue::value)
    ).apply(instance, StringValue::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return this.value();
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
