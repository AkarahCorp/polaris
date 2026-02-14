package dev.akarah.polaris.building.region;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.MapCodec;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Unit;

import java.util.List;

public record NoopRegion() implements Region {
    public static MapCodec<NoopRegion> CODEC = Unit.CODEC.xmap(_ -> new NoopRegion(), _ -> Unit.INSTANCE).fieldOf("noop");

    @Override
    public void forEach(ServerLevel level, BlockPos target, RegionLoop loop) {

    }

    @Override
    public MapCodec<? extends Region> codec() {
        return CODEC;
    }

    @Override
    public List<Component> description() {
        return List.of(
                Component.literal("Nothing").withColor(ARGB.color(255, 0, 0))
        );
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext context) {

    }
}
