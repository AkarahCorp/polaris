package dev.akarah.cdata.script.action.player;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.script.action.ActionProvider;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;
import dev.akarah.cdata.script.value.values.constants.NumberValue;
import net.minecraft.core.Holder;

public record GiveItemAction(
        ValueProvider item,
        ValueProvider amount
) implements ActionProvider {
    public static MapCodec<GiveItemAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ValueProvider.CUSTOM_ITEM_CODEC.fieldOf("item").forGetter(GiveItemAction::item),
            ValueProvider.NUMBER_CODEC.optionalFieldOf("amount", new NumberValue(1.0)).forGetter(GiveItemAction::amount)
    ).apply(instance, GiveItemAction::new));

    @Override
    public void execute(ScriptContext ctx) {
        ctx.defaultSelection().forEachPlayer(player -> {
            item.asCustomItem(ctx)
                    .map(Holder::value)
                    .map(CustomItem::toItemStack)
                    .ifPresent(itemStack -> {
                itemStack.setCount((int) amount.asNumber(ctx));
                player.addItem(itemStack);
            });
        });
    }

    @Override
    public MapCodec<? extends ActionProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
