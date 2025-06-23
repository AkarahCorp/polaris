package dev.akarah.cdata.script.action.player;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.action.ActionProvider;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

import java.util.List;

public record RepeatTimesAction(
        ValueProvider times,
        List<ActionProvider> actions
) implements ActionProvider {
    public static MapCodec<RepeatTimesAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ValueProvider.NUMBER_CODEC.fieldOf("times").forGetter(RepeatTimesAction::times),
            ActionProvider.CODEC.listOf().fieldOf("actions").forGetter(RepeatTimesAction::actions)
    ).apply(instance, RepeatTimesAction::new));

    @Override
    public void execute(ScriptContext ctx) {
        var times = this.times.evaluate(ctx, Double.class).intValue();
        for(int i = 0; i < times; i++) {
            this.actions.forEach(x -> x.execute(ctx));
        }
    }

    @Override
    public MapCodec<? extends ActionProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
