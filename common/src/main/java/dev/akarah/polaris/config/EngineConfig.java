package dev.akarah.polaris.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.stat.StatsObject;

import java.util.Optional;

public record EngineConfig(
        StatsObject baseStats,
        Optional<FishingConfig> fishingConfig,
        String serverType
) {
    public static Codec<EngineConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StatsObject.CODEC.optionalFieldOf("base_stats", StatsObject.EMPTY).forGetter(EngineConfig::baseStats),
            FishingConfig.CODEC.optionalFieldOf("fishing").forGetter(EngineConfig::fishingConfig),
            Codec.STRING.optionalFieldOf("server_type", "unknown").forGetter(EngineConfig::serverType)
    ).apply(instance, EngineConfig::new));
}
