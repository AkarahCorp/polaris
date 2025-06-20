package dev.akarah.cdata.registry.citem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;

public record CustomItem(
        ResourceLocation baseItem,
        DataComponentMap map
) {
    public static Codec<CustomItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("base_item").forGetter(CustomItem::baseItem),
            DataComponentMap.CODEC.fieldOf("components").forGetter(CustomItem::map)
    ).apply(instance, CustomItem::new));
}
