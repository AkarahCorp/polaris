package dev.akarah.polaris.building.palette;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.building.wand.WandOperations;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SingleBiomePalette(ResourceKey<@NotNull Biome> biome) implements Palette {
    public static MapCodec<SingleBiomePalette> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
           ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(SingleBiomePalette::biome)
    ).apply(instance, SingleBiomePalette::new));

    @Override
    public MapCodec<? extends Palette> codec() {
        return CODEC;
    }

    @Override
    public void apply(ServerLevel level, BlockPos blockPos) {
        level.getChunk(blockPos).fillBiomesFromNoise(
                (x, y, z, sampler) -> level.registryAccess().getOrThrow(this.biome),
                Climate.empty()
        );
    }

    @Override
    public List<Component> description() {
        return List.of(
                Component.literal("Applies biome ").withColor(ARGB.color(200, 200, 200))
                        .append(Component.literal(biome.identifier().toString()).withColor(ARGB.color(255, 255, 0)))
        );
    }



    public static void register(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext context) {
        node.then(
                Commands.literal("biome").then(
                        Commands.argument("biome", ResourceKeyArgument.key(Registries.BIOME))
                                .executes(ctx -> {
                                    WandOperations.applyToWand(
                                            ctx.getSource().getEntity(),
                                            WandOperations.addPalette(new SingleBiomePalette(
                                                    ResourceKeyArgument.getRegistryKey(
                                                            ctx, "biome", Registries.BIOME, new DynamicCommandExceptionType(a -> new LiteralMessage("uh oh bad key?"))
                                                    )
                                            ))
                                    );
                                    return 0;
                                })
                )
        );
    }
}
