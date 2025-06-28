package dev.akarah.cdata.script.action.player;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.text.ParseContext;
import dev.akarah.cdata.script.action.ActionProvider;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

public record SendActionBarAction(
        ValueProvider message
) implements ActionProvider {
    public static MapCodec<SendActionBarAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ValueProvider.TEXT_VALUE.fieldOf("messagewd").forGetter(SendActionBarAction::message)
    ).apply(instance, SendActionBarAction::new));

    @Override
    public void execute(ScriptContext ctx) {
        ctx.defaultSelection().forEachPlayer(player -> {
            var parseContext = ParseContext.empty();
            message.asText(ctx)
                    .output(parseContext)
                    .ifPresent(component -> player.sendSystemMessage(component, true));
        });
    }

    @Override
    public MapCodec<? extends ActionProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
