package dev.akarah.cdata.registry.citem;

import com.mojang.serialization.Codec;
import dev.akarah.cdata.property.PropertyMap;

public record CustomItem(
        PropertyMap properties
) {
    public static Codec<CustomItem> CODEC = PropertyMap.CODEC.xmap(CustomItem::new, CustomItem::properties);
}
