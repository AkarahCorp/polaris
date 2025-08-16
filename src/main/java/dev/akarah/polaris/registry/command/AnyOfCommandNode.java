package dev.akarah.polaris.registry.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

public record AnyOfCommandNode(List<CommandBuilderNode> children) implements CommandBuilderNode {
    public static MapCodec<AnyOfCommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CommandBuilderNode.CODEC.listOf().fieldOf("choices").forGetter(AnyOfCommandNode::children)
    ).apply(instance, AnyOfCommandNode::new));

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node) {
        for(var child : children) {
            child.dispatch(node);
        }
        return node;
    }

    @Override
    public MapCodec<? extends CommandBuilderNode> codec() {
        return CODEC;
    }
}
