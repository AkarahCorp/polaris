package dev.akarah.polaris.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.polaris.registry.achievement.CriteriaObject;
import dev.akarah.polaris.registry.command.ArgumentTypes;
import dev.akarah.polaris.registry.command.CommandBuilderNode;
import dev.akarah.polaris.registry.entity.behavior.TaskType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;

import java.util.List;

public class ExtBuiltInRegistries {
    public static Registry<MapCodec<? extends TaskType>> BEHAVIOR_TYPE;
    public static Registry<MapCodec<? extends CommandBuilderNode>> COMMAND_NODE_TYPE;
    public static Registry<ArgumentType<?>> ARGUMENT_TYPES;
    public static Registry<Codec<CriteriaObject>> CRITERIA_TYPE;

    public static void bootStrap() {
        ExtBuiltInRegistries.BEHAVIOR_TYPE = BuiltInRegistries.registerSimple(ExtRegistries.BEHAVIOR_TYPE, TaskType::bootStrap);
        ExtBuiltInRegistries.COMMAND_NODE_TYPE = BuiltInRegistries.registerSimple(ExtRegistries.COMMAND_NODE_TYPE, CommandBuilderNode::bootStrap);
        ExtBuiltInRegistries.ARGUMENT_TYPES = BuiltInRegistries.registerSimple(ExtRegistries.ARGUMENT_TYPE, ArgumentTypes::bootStrap);
        ExtBuiltInRegistries.CRITERIA_TYPE = BuiltInRegistries.registerDefaulted(ExtRegistries.CRITERIA_TYPE, "polaris:dynamic", CriteriaObject::bootStrapCriteriaTypes);
    }

    public static List<RegistryDataLoader.RegistryData<?>> DYNAMIC_REGISTRIES = List.of();
}
