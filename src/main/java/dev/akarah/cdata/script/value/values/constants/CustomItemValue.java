package dev.akarah.cdata.script.value.values.constants;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;
import net.minecraft.core.Holder;

public record CustomItemValue(Holder<CustomItem> item) implements ValueProvider {
    public static MapCodec<CustomItemValue> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CustomItem.HOLDER_CODEC.fieldOf("item").forGetter(CustomItemValue::item)
    ).apply(instance, CustomItemValue::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return this.item();
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
