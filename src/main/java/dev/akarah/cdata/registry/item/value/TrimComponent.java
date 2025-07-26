package dev.akarah.cdata.registry.item.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record TrimComponent(
        ResourceLocation material,
        ResourceLocation pattern
) {
    public static Codec<TrimComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("material").forGetter(TrimComponent::material),
            ResourceLocation.CODEC.fieldOf("pattern").forGetter(TrimComponent::pattern)
    ).apply(instance, TrimComponent::new));
}
