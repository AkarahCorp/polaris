package dev.akarah.polaris.building.palette;

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

public record NoopPalette() implements Palette {
    public static MapCodec<NoopPalette> CODEC = Unit.CODEC.xmap(_ -> new NoopPalette(), _ -> Unit.INSTANCE).fieldOf("noop");

    @Override
    public MapCodec<? extends Palette> codec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevel level, BlockPos blockPos) {
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
