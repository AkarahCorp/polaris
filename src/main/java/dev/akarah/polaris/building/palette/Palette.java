package dev.akarah.polaris.building.palette;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.polaris.building.region.CubeRegion;
import dev.akarah.polaris.building.region.MultiRegion;
import dev.akarah.polaris.building.region.NoopRegion;
import dev.akarah.polaris.building.region.SphereRegion;
import dev.akarah.polaris.registry.ExtBuiltInRegistries;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;

public sealed interface Palette permits SingleBiomePalette, MultiPalette, NoopPalette, SingleBlockPalette {
    Codec<Palette> CODEC = ExtBuiltInRegistries.PALETTE_TYPES.byNameCodec()
        .dispatch(Palette::codec, x -> x);

    MapCodec<? extends Palette> codec();
    void apply(ServerLevel level, BlockPos blockPos);
    List<Component> description();

    List<BiConsumer<LiteralArgumentBuilder<CommandSourceStack>, CommandBuildContext>> COMMAND_REGISTERS = List.of(
            NoopPalette::register,
            MultiPalette::register,
            SingleBlockPalette::register,
            SingleBiomePalette::register
    );

    static Object bootStrap(Registry<@NotNull MapCodec<? extends Palette>> registry) {
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("stacked"),
                MultiPalette.CODEC
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("single_block"),
                SingleBlockPalette.CODEC
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("noop"),
                NoopPalette.CODEC
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("biome"),
                SingleBiomePalette.CODEC
        );
        return NoopPalette.CODEC;
    }
}
