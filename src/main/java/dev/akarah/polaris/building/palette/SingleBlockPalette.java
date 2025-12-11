package dev.akarah.polaris.building.palette;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.building.wand.WandOperations;
import dev.akarah.polaris.building.wand.WandTasks;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record SingleBlockPalette(BlockState blockState) implements Palette {
    public static MapCodec<SingleBlockPalette> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
           BlockState.CODEC.fieldOf("block").forGetter(SingleBlockPalette::blockState)
    ).apply(instance, SingleBlockPalette::new));

    @Override
    public MapCodec<? extends Palette> codec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevel level, BlockPos blockPos) {
        level.setBlock(blockPos, this.blockState, 2);
    }

    @Override
    public List<Component> description() {
        return List.of(
                Component.literal("Applies block ").withColor(ARGB.color(200, 200, 200))
                        .append(Component.literal(blockState.toString()).withColor(ARGB.color(255, 255, 0)))
        );
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext context) {
        node.then(
                Commands.literal("block").then(
                        Commands.argument("block", BlockStateArgument.block(context))
                                .executes(ctx -> {
                                    WandOperations.applyToWand(
                                            ctx.getSource().getEntity(),
                                            WandOperations.addPalette(new SingleBlockPalette(
                                                    BlockStateArgument.getBlock(ctx, "block").getState()
                                            ))
                                    );
                                    return 0;
                                })
                )
        );
    }
}
