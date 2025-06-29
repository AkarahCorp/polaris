package dev.akarah.cdata.script.action.env;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.action.ActionProvider;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

import java.util.List;

public record SetLocalAction(
        String variableName,
        ValueProvider value
) implements ActionProvider {
    public static MapCodec<SetLocalAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("var").forGetter(SetLocalAction::variableName),
            ValueProvider.CODEC.fieldOf("value").forGetter(SetLocalAction::value)
    ).apply(instance, SetLocalAction::new));

    @Override
    public void execute(ScriptContext ctx) {
        ctx.localVariables().put(
                this.variableName,
                this.value.evaluate(ctx)
        );
    }

    @Override
    public MapCodec<? extends ActionProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
