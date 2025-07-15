package dev.akarah.cdata.registry.mining;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.List;

public record MiningRule(
        List<Block> materials,
        Pair<BlockPos, BlockPos> area,
        double toughness,
        String speedStat,
        String spreadStat
) {
    public static Codec<MiningRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("materials").forGetter(MiningRule::materials),
            Codec.pair(BlockPos.CODEC, BlockPos.CODEC)
                    .optionalFieldOf("area", Pair.of(new BlockPos(-30000000, -30000000, -30000000), new BlockPos(30000000, 30000000, 30000000)))
                    .forGetter(MiningRule::area),
            Codec.DOUBLE.fieldOf("toughness").forGetter(MiningRule::toughness),
            Codec.STRING.fieldOf("speed_stat").forGetter(MiningRule::speedStat),
            Codec.STRING.optionalFieldOf("spread_stat", "?").forGetter(MiningRule::spreadStat)
    ).apply(instance, MiningRule::new));
}
