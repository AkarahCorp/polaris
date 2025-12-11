package dev.akarah.polaris.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.polaris.building.palette.Palette;
import dev.akarah.polaris.building.region.Region;
import dev.akarah.polaris.registry.achievement.CriteriaObject;
import dev.akarah.polaris.registry.command.CommandBuilderNode;
import dev.akarah.polaris.registry.entity.behavior.TaskType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ExtRegistries {
    public static ResourceKey<@NotNull Registry<@NotNull MapCodec<? extends TaskType>>> BEHAVIOR_TYPE =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("engine", "behavior/type"));
    public static ResourceKey<@NotNull Registry<@NotNull MapCodec<? extends CommandBuilderNode>>> COMMAND_NODE_TYPE =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("engine", "command/node_type"));
    public static ResourceKey<@NotNull Registry<@NotNull ArgumentType<?>>> ARGUMENT_TYPE =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("engine", "command/argument_type"));
    public static ResourceKey<@NotNull Registry<@NotNull Codec<CriteriaObject>>> CRITERIA_TYPE =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("engine", "achievement/criteria/type"));
    public static ResourceKey<@NotNull Registry<@NotNull MapCodec<? extends Palette>>> PALETTE_TYPE =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("engine", "ps/palette"));
    public static ResourceKey<@NotNull Registry<@NotNull MapCodec<? extends Region>>> REGION_TYPE =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("engine", "ps/region"));
}
