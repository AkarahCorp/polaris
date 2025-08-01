package dev.akarah.cdata.registry.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface CommandBuilderNode {
    public static Codec<CommandBuilderNode> CODEC = Codec.lazyInitialized(() ->
            ExtBuiltInRegistries.COMMAND_NODE_TYPE
                    .byNameCodec()
                    .dispatch(CommandBuilderNode::codec, x -> x)
    );

    ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node);

    MapCodec<? extends CommandBuilderNode> codec();

    static Object bootstrap(Registry<MapCodec<? extends CommandBuilderNode>> registry) {
        Registry.register(registry, "literal", LiteralCommandNode.CODEC);
        Registry.register(registry, "executes", ExecutesCommandNode.CODEC);
        return LiteralCommandNode.CODEC;
    }
}
