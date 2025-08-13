package dev.akarah.cdata.registry.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class ArgumentTypes {
    public static Object bootStrap(Registry<ArgumentType<?>> registry) {
        Registry.register(
                registry,
                ResourceLocation.withDefaultNamespace("bool"),
                BoolArgumentType.bool()
        );
        Registry.register(
                registry,
                ResourceLocation.withDefaultNamespace("string"),
                StringArgumentType.string()
        );
        Registry.register(
                registry,
                ResourceLocation.withDefaultNamespace("double"),
                DoubleArgumentType.doubleArg()
        );
        Registry.register(
                registry,
                ResourceLocation.withDefaultNamespace("greedy_string"),
                StringArgumentType.greedyString()
        );
        Registry.register(
                registry,
                ResourceLocation.withDefaultNamespace("word"),
                StringArgumentType.word()
        );
        Registry.register(
                registry,
                ResourceLocation.withDefaultNamespace("entity"),
                EntityArgument.entity()
        );
        Registry.register(
                registry,
                ResourceLocation.withDefaultNamespace("entities"),
                EntityArgument.entities()
        );
        Registry.register(
                registry,
                ResourceLocation.withDefaultNamespace("player"),
                EntityArgument.player()
        );
        Registry.register(
                registry,
                ResourceLocation.withDefaultNamespace("players"),
                EntityArgument.players()
        );
        return DoubleArgumentType.doubleArg();
    }
}
