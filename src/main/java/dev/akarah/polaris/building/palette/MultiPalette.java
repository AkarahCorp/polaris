package dev.akarah.polaris.building.palette;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.building.region.CubeRegion;
import dev.akarah.polaris.building.wand.WandOperations;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public record MultiPalette(Palette first, Palette second) implements Palette {
    public static MapCodec<MultiPalette> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Palette.CODEC.fieldOf("first").forGetter(MultiPalette::first),
            Palette.CODEC.fieldOf("second").forGetter(MultiPalette::second)
    ).apply(instance, MultiPalette::new));

    @Override
    public MapCodec<? extends Palette> codec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevel level, BlockPos blockPos) {
        first.apply(level, blockPos);
        second.apply(level, blockPos);
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
