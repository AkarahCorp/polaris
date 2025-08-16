package dev.akarah.polaris.registry.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.polaris.registry.ExtBuiltInRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;

public interface CommandBuilderNode {
    public static Codec<CommandBuilderNode> CODEC = Codec.lazyInitialized(() ->
            ExtBuiltInRegistries.COMMAND_NODE_TYPE
                    .byNameCodec()
                    .dispatch(CommandBuilderNode::codec, x -> x)
    );

    ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node);

    MapCodec<? extends CommandBuilderNode> codec();

    static Object bootStrap(Registry<MapCodec<? extends CommandBuilderNode>> registry) {
        Registry.register(registry, "literal", LiteralCommandNode.CODEC);
        Registry.register(registry, "executes", ExecutesCommandNode.CODEC);
        Registry.register(registry, "argument", ArgumentCommandNode.CODEC);
        Registry.register(registry, "any_of", AnyOfCommandNode.CODEC);
        return LiteralCommandNode.CODEC;
    }
}
