package dev.akarah.cdata.script.value.values.constants;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

public record TextLineValue(ParsedText line) implements ValueProvider {
    public static MapCodec<TextLineValue> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ParsedText.CODEC.fieldOf("contents").forGetter(TextLineValue::line)
    ).apply(instance, TextLineValue::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return this.line();
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
