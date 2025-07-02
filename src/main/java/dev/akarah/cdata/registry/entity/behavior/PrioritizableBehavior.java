package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PrioritizableBehavior(
        int priority,
        Behavior behavior
) {
    public static Codec<PrioritizableBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("priority").forGetter(PrioritizableBehavior::priority),
            Behavior.CODEC.fieldOf("behavior").forGetter(PrioritizableBehavior::behavior)
    ).apply(instance, PrioritizableBehavior::new));
}
