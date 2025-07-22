package dev.akarah.cdata.registry.mining;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.loot.LootTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MiningRule(
        List<Block> materials,
        Map<String, String> stateRequirements,
        Pair<BlockPos, BlockPos> area,
        double toughness,
        String speedStat,
        String spreadStat,
        Optional<LootTable> lootTable
) {
    public static Codec<MiningRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("materials").forGetter(MiningRule::materials),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("block_state", Map.of()).forGetter(MiningRule::stateRequirements),
            Codec.pair(BlockPos.CODEC, BlockPos.CODEC)
                    .optionalFieldOf("area", Pair.of(new BlockPos(-30000000, -30000000, -30000000), new BlockPos(30000000, 30000000, 30000000)))
                    .forGetter(MiningRule::area),
            Codec.DOUBLE.fieldOf("toughness").forGetter(MiningRule::toughness),
            Codec.STRING.fieldOf("speed_stat").forGetter(MiningRule::speedStat),
            Codec.STRING.optionalFieldOf("spread_stat", "?").forGetter(MiningRule::spreadStat),
            LootTable.CODEC.optionalFieldOf("loot_table").forGetter(MiningRule::lootTable)
    ).apply(instance, MiningRule::new));
}
