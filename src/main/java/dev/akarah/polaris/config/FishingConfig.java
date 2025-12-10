package dev.akarah.polaris.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.loot.LootTable;
import net.minecraft.resources.Identifier;

public record FishingConfig(
        int baseLureTime,
        int baseHookTime,
        Identifier lureSpeedStat,
        Identifier hookSpeedStat,
        LootTable lootTable
) {
    public static Codec<FishingConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("base_lure_time").forGetter(FishingConfig::baseLureTime),
            Codec.INT.fieldOf("base_hook_time").forGetter(FishingConfig::baseHookTime),
            Identifier.CODEC.fieldOf("lure_speed_stat").forGetter(FishingConfig::lureSpeedStat),
            Identifier.CODEC.fieldOf("hook_speed_stat").forGetter(FishingConfig::hookSpeedStat),
            LootTable.CODEC.fieldOf("loot_table").forGetter(FishingConfig::lootTable)
    ).apply(instance, FishingConfig::new));
}
