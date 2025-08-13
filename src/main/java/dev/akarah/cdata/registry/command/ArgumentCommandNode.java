package dev.akarah.cdata.registry.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;

public record ArgumentCommandNode(String key, Holder<ArgumentType<?>> argumentTypeHolder, CommandBuilderNode then) implements CommandBuilderNode {
    public static MapCodec<ArgumentCommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(ArgumentCommandNode::key),
            ExtBuiltInRegistries.ARGUMENT_TYPES.holderByNameCodec()
                    .fieldOf("argument_type")
                    .forGetter(ArgumentCommandNode::argumentTypeHolder),
            CommandBuilderNode.CODEC.fieldOf("then").forGetter(ArgumentCommandNode::then)
    ).apply(instance, ArgumentCommandNode::new));

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node) {
        var argumentNode = Commands.argument(key, argumentTypeHolder.value());
        then.dispatch(argumentNode);
        node.then(argumentNode);
        return node;
    }

    @Override
    public MapCodec<? extends CommandBuilderNode> codec() {
        return CODEC;
    }
}
