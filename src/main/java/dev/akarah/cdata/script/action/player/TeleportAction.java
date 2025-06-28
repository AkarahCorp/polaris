package dev.akarah.cdata.script.action.player;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.script.action.ActionProvider;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;

public record TeleportAction(
        ValueProvider location
) implements ActionProvider {
    public static MapCodec<TeleportAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ValueProvider.VEC3_CODEC.fieldOf("location").forGetter(TeleportAction::location)
    ).apply(instance, TeleportAction::new));

    @Override
    public void execute(ScriptContext ctx) {
        ctx.defaultSelection().forEach(entity -> {
            var vec = location.asVec3(ctx);
            entity.teleportTo(vec.x, vec.y, vec.z);
        });
    }

    @Override
    public MapCodec<? extends ActionProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
