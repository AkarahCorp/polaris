package dev.akarah.polaris.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.polaris.registry.achievement.CriteriaObject;
import dev.akarah.polaris.registry.command.CommandBuilderNode;
import dev.akarah.polaris.registry.entity.behavior.TaskType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ExtRegistries {
    public static ResourceKey<Registry<MapCodec<? extends TaskType>>> BEHAVIOR_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "behavior/type"));
    public static ResourceKey<Registry<MapCodec<? extends CommandBuilderNode>>> COMMAND_NODE_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "command/node_type"));
    public static ResourceKey<Registry<ArgumentType<?>>> ARGUMENT_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "command/argument_type"));
    public static ResourceKey<Registry<Codec<CriteriaObject>>> CRITERIA_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "achievement/criteria/type"));
}
