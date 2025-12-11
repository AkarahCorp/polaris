package dev.akarah.polaris.building.region;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.building.wand.WandOperations;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;

import java.util.List;

public record SphereRegion(int radius) implements Region {
    public static MapCodec<SphereRegion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("radius").forGetter(SphereRegion::radius)
    ).apply(instance, SphereRegion::new));

    @Override
    public void forEach(ServerLevel level, BlockPos target, RegionLoop loop) {
        var radiusSquared = Math.pow(radius, 2);
        for(int x = -radius; x <= radius; x++) {
            for(int y = -radius; y <= radius; y++) {
                for(int z =-radius; z <= radius; z++) {
                    var offset = target.offset(x, y, z);
                    if(target.distSqr(offset) <= radiusSquared) {
                        loop.apply(level, offset);
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends Region> codec() {
        return CODEC;
    }

    @Override
    public List<Component> description() {
        return List.of(
                Component.literal("Sphere of radius ").withColor(ARGB.color(200, 200, 200))
                        .append(Component.literal(String.valueOf(radius)).withColor(ARGB.color(255, 255, 0)))
        );
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> node, CommandBuildContext context) {
        node.then(
                Commands.literal("sphere").then(
                        Commands.argument("radius", IntegerArgumentType.integer(1, 20))
                                .executes(ctx -> {
                                    WandOperations.applyToWand(
                                            ctx.getSource().getEntity(),
                                            WandOperations.addRegion(new SphereRegion(IntegerArgumentType.getInteger(ctx, "radius")))
                                    );
                                    return 0;
                                })
                )
        );
    }
}
