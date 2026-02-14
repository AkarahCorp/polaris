package dev.akarah.polaris.registry.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.stat.StatsObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record CustomEffect(
        Component name,
        int maxLevel,
        Optional<EffectStats> stats,
        Optional<Identifier> tickingFunction
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
            ComponentSerialization.CODEC.fieldOf("name").forGetter(CustomEffect::name),
            Codec.INT.fieldOf("maxLevel").forGetter(CustomEffect::maxLevel),
            EffectStats.CODEC.optionalFieldOf("stats").forGetter(CustomEffect::stats),
            Identifier.CODEC.optionalFieldOf("ticking_function").forGetter(CustomEffect::tickingFunction)
            ).apply(instance, CustomEffect::new)
    );
}
