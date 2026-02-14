package dev.akarah.polaris.registry.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.Scheduler;
import dev.akarah.polaris.registry.ExtBuiltInRegistries;
import dev.akarah.polaris.registry.Resources;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;

import java.util.List;
import java.util.Set;

public record ArgumentCommandNode(String key, Holder<ArgumentType<?>> argumentTypeHolder, CommandBuilderNode then) implements CommandBuilderNode {
    public record ArgumentPair(String key) {}

    public static Set<ArgumentPair> ARGUMENT_KEYS = Sets.newHashSet();

    public static MapCodec<ArgumentCommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(ArgumentCommandNode::key),
            ExtBuiltInRegistries.ARGUMENT_TYPES.holderByNameCodec()
                    .fieldOf("argument_type")
                    .forGetter(ArgumentCommandNode::argumentTypeHolder),
            CommandBuilderNode.CODEC.fieldOf("then").forGetter(ArgumentCommandNode::then)
    ).apply(instance, ArgumentCommandNode::new));

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node) {
        ARGUMENT_KEYS.add(new ArgumentPair(this.key()));
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
