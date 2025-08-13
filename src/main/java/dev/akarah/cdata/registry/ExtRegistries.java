package dev.akarah.cdata.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.command.CommandBuilderNode;
import dev.akarah.cdata.registry.entity.behavior.TaskType;
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
}
