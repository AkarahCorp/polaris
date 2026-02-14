package dev.akarah.polaris.registry.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StatType(
        String name,
        String color,
        double baseValue
) {
    public static Codec<StatType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(StatType::name),
            Codec.STRING.fieldOf("color").forGetter(StatType::color),
            Codec.DOUBLE.fieldOf("base_value").forGetter(StatType::baseValue)
    ).apply(instance, StatType::new));
}
