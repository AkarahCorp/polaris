package dev.akarah.polaris.building.region;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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

public sealed interface Region permits CubeRegion, MultiRegion, NoopRegion, SphereRegion {
    Codec<Region> CODEC = Codec.lazyInitialized(() -> ExtBuiltInRegistries.REGION_TYPES.byNameCodec()
            .dispatch(Region::codec, x -> x));

    interface RegionLoop {
        void apply(ServerLevel level, BlockPos blockPos);
    }

    void forEach(ServerLevel level, BlockPos target, RegionLoop loop);
    MapCodec<? extends Region> codec();
    List<Component> description();

    List<BiConsumer<LiteralArgumentBuilder<CommandSourceStack>, CommandBuildContext>> COMMAND_REGISTERS = List.of(
            NoopRegion::register,
            MultiRegion::register,
            SphereRegion::register,
            CubeRegion::register
    );

    static Object bootStrap(Registry<@NotNull MapCodec<? extends Region>> registry) {
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("cube"),
                CubeRegion.CODEC
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("multi"),
                MultiRegion.CODEC
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("noop"),
                NoopRegion.CODEC
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("sphere"),
                SphereRegion.CODEC
        );
        return NoopRegion.CODEC;
    }
}
