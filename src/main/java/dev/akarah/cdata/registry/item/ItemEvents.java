package dev.akarah.cdata.registry.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record ItemEvents(
        Optional<List<ResourceLocation>> onRightClick,
        Optional<List<ResourceLocation>> onLeftClick,
        Optional<List<ResourceLocation>> onSneak,
        Optional<List<ResourceLocation>> onMenuClick
) {
    public static Codec<ItemEvents> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("right_click").forGetter(ItemEvents::onRightClick),
            ResourceLocation.CODEC.listOf().optionalFieldOf("left_click").forGetter(ItemEvents::onLeftClick),
            ResourceLocation.CODEC.listOf().optionalFieldOf("sneak").forGetter(ItemEvents::onSneak),
            ResourceLocation.CODEC.listOf().optionalFieldOf("menu_click").forGetter(ItemEvents::onMenuClick)
    ).apply(instance, ItemEvents::new)));
}
