package dev.akarah.cdata.script.action.player;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.action.ActionProvider;
import dev.akarah.cdata.script.env.ScriptContext;

import java.util.List;

public record AllOfAction(
        List<ActionProvider> actions
) implements ActionProvider {
    public static MapCodec<AllOfAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ActionProvider.CODEC.listOf().fieldOf("actions").forGetter(AllOfAction::actions)
    ).apply(instance, AllOfAction::new));

    @Override
    public void execute(ScriptContext ctx) {
        this.actions.forEach(x -> x.execute(ctx));
    }

    @Override
    public MapCodec<? extends ActionProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
