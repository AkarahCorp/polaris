package dev.akarah.cdata.script.value.values.env;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

public record LocalVarValue(String variableName) implements ValueProvider {
    public static MapCodec<LocalVarValue> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("var").forGetter(LocalVarValue::variableName)
    ).apply(instance, LocalVarValue::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return ctx.localVariables().lookup(this.variableName);
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
