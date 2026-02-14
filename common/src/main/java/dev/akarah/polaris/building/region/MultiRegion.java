package dev.akarah.polaris.building.region;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public record MultiRegion(Region first, Region second) implements Region {
    public static MapCodec<MultiRegion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Region.CODEC.fieldOf("first").forGetter(MultiRegion::first),
            Region.CODEC.fieldOf("second").forGetter(MultiRegion::second)
    ).apply(instance, MultiRegion::new));

    @Override
    public void forEach(ServerLevel level, BlockPos target, RegionLoop loop) {
        first.forEach(level, target, loop);
        second.forEach(level, target, loop);
    }

    @Override
    public MapCodec<? extends Region> codec() {
        return CODEC;
    }

    @Override
    public List<Component> description() {
        var l = new ArrayList<Component>();
        l.addAll(first.description());
        l.addAll(second.description());
        return l;
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext context) {

    }
}
