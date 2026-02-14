package dev.akarah.polaris.registry.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class ArgumentTypes {
    public static Object bootStrap(Registry<ArgumentType<?>> registry) {
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("bool"),
                BoolArgumentType.bool()
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("string"),
                StringArgumentType.string()
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("double"),
                DoubleArgumentType.doubleArg()
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("greedy_string"),
                StringArgumentType.greedyString()
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("word"),
                StringArgumentType.word()
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("entity"),
                EntityArgument.entity()
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("entities"),
                EntityArgument.entities()
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("player"),
                EntityArgument.player()
        );
        Registry.register(
                registry,
                Identifier.withDefaultNamespace("players"),
                EntityArgument.players()
        );
        return DoubleArgumentType.doubleArg();
    }
}
