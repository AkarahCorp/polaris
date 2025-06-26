package dev.akarah.cdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.stat.StatsObject;

public record EngineConfig(
        StatsObject baseStats
) {
    public static Codec<EngineConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StatsObject.CODEC.optionalFieldOf("base_stats", StatsObject.EMPTY).forGetter(EngineConfig::baseStats)
    ).apply(instance, EngineConfig::new));
}
