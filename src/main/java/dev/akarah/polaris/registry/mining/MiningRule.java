package dev.akarah.polaris.registry.mining;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.loot.LootTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MiningRule(
        List<Block> materials,
        Map<String, String> stateRequirements,
        Pair<BlockPos, BlockPos> area,
        double toughness,
        Optional<Identifier> spreadToughnessFunction,
        Identifier speedStat,
        Identifier spreadStat,
        Optional<LootTable> lootTable,
        double spreadToughness,
        Optional<RegenerationRule> regeneration,
        Optional<BreakingPowerRule> breakingPower
) {
    public record RegenerationRule(
            Block replacement,
            int delay
    ) {
        public static Codec<RegenerationRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("replacement").forGetter(RegenerationRule::replacement),
                Codec.INT.fieldOf("delay").forGetter(RegenerationRule::delay)
        ).apply(instance, RegenerationRule::new));
    }

    public record BreakingPowerRule(
            Identifier stat,
            double minimumAmount
    ) {
        public static Codec<BreakingPowerRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("stat").forGetter(BreakingPowerRule::stat),
                Codec.DOUBLE.fieldOf("requirement").forGetter(BreakingPowerRule::minimumAmount)
        ).apply(instance, BreakingPowerRule::new));
    }

    public static Codec<MiningRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("materials").forGetter(MiningRule::materials),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("block_state", Map.of()).forGetter(MiningRule::stateRequirements),
            Codec.pair(BlockPos.CODEC.fieldOf("min").codec(), BlockPos.CODEC.fieldOf("max").codec())
                    .optionalFieldOf("area", Pair.of(new BlockPos(-30000000, -30000000, -30000000), new BlockPos(30000000, 30000000, 30000000)))
                    .forGetter(MiningRule::area),
            Codec.DOUBLE.fieldOf("toughness").forGetter(MiningRule::toughness),
            Identifier.CODEC.optionalFieldOf("spread_toughness_function").forGetter(MiningRule::spreadToughnessFunction),
            Identifier.CODEC.fieldOf("speed_stat").forGetter(MiningRule::speedStat),
            Identifier.CODEC.optionalFieldOf("spread_stat", Identifier.withDefaultNamespace("unknown")).forGetter(MiningRule::spreadStat),
            LootTable.CODEC.optionalFieldOf("loot_table").forGetter(MiningRule::lootTable),
            Codec.DOUBLE.optionalFieldOf("spread_toughness", 0.0).forGetter(MiningRule::spreadToughness),
            RegenerationRule.CODEC.optionalFieldOf("regeneration").forGetter(MiningRule::regeneration),
            BreakingPowerRule.CODEC.optionalFieldOf("breaking_power").forGetter(MiningRule::breakingPower)
    ).apply(instance, MiningRule::new));
}
