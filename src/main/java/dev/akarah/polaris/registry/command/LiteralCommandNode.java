package dev.akarah.polaris.registry.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public record LiteralCommandNode(String literal, CommandBuilderNode then) implements CommandBuilderNode {
    public static MapCodec<LiteralCommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("literal").forGetter(LiteralCommandNode::literal),
            CommandBuilderNode.CODEC.fieldOf("then").forGetter(LiteralCommandNode::then)
    ).apply(instance, LiteralCommandNode::new));

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node) {
        var literalNode = Commands.literal(this.literal);
        this.then.dispatch(literalNode);
        node.then(literalNode);
        return node;
    }

    @Override
    public MapCodec<? extends CommandBuilderNode> codec() {
        return CODEC;
    }
}
