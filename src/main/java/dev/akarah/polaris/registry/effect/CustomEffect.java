package dev.akarah.polaris.registry.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.stat.StatsObject;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public record CustomEffect(
        String name,
        int maxLevel,
        Optional<EffectStats> stats,
        Optional<ResourceLocation> tickingFunction
) {

    public record EffectStats(
            Optional<StatsObject> base,
            Optional<StatsObject> perLevel
    ) {
        public static EffectStats EMPTY = new EffectStats(null, null);
        public static final Codec<EffectStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                StatsObject.CODEC.optionalFieldOf("base").forGetter(EffectStats::base),
                StatsObject.CODEC.optionalFieldOf("per_level").forGetter(EffectStats::perLevel)
        ).apply(instance, EffectStats::new));
    }

    public static final Codec<CustomEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(CustomEffect::name),
            Codec.INT.fieldOf("maxLevel").forGetter(CustomEffect::maxLevel),
            EffectStats.CODEC.optionalFieldOf("stats").forGetter(CustomEffect::stats),
            ResourceLocation.CODEC.optionalFieldOf("ticking_function").forGetter(CustomEffect::tickingFunction)
            ).apply(instance, CustomEffect::new)
    );
}
