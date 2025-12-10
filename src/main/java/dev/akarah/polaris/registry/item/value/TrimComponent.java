package dev.akarah.polaris.registry.item.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record TrimComponent(
        Identifier material,
        Identifier pattern
) {
    public static Codec<TrimComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("material").forGetter(TrimComponent::material),
            Identifier.CODEC.fieldOf("pattern").forGetter(TrimComponent::pattern)
    ).apply(instance, TrimComponent::new));
}
