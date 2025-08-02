package dev.akarah.cdata.registry.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

public record LiteralCommandNode(String literal, List<CommandBuilderNode> children) implements CommandBuilderNode {
    public static MapCodec<LiteralCommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("literal").forGetter(LiteralCommandNode::literal),
            CommandBuilderNode.CODEC.listOf().fieldOf("then").forGetter(LiteralCommandNode::children)
    ).apply(instance, LiteralCommandNode::new));

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node) {
        var literalNode = Commands.literal(this.literal);
        for(var child : this.children) {
            child.dispatch(literalNode);
        }
        node.then(literalNode);
        return node;
    }

    @Override
    public MapCodec<? extends CommandBuilderNode> codec() {
        return CODEC;
    }
}
