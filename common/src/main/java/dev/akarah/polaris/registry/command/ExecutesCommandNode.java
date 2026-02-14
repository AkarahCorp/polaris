package dev.akarah.polaris.registry.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.RDict;
import dev.akarah.polaris.script.value.RString;
import dev.akarah.polaris.script.value.RuntimeValue;
import dev.akarah.polaris.script.value.mc.REntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Map;

public record ExecutesCommandNode(Identifier action) implements CommandBuilderNode {
    public static MapCodec<ExecutesCommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("action").forGetter(ExecutesCommandNode::action)
    ).apply(instance, ExecutesCommandNode::new));


    @Override
    public ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node) {
        return node.executes(ctx -> {
            try {
                var obj = RDict.create();

                for(var argument : ArgumentCommandNode.ARGUMENT_KEYS) {
                    try {
                        var result = ctx.getArgument(argument.key(), Object.class);
                        if(result == null) {
                            throw new Exception("Result is null and doesn't exist!");
                        }
                        RDict.put(
                                obj,
                                RString.of(argument.key()),
                                RuntimeValue.from(result, ctx.getSource())
                        );
                    } catch (Exception ignored) {
                    }
                }

                var returns = Resources.actionManager().executeBoolean(action, REntity.of(ctx.getSource().getEntityOrException()), obj);
                return returns ? 1 : 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    @Override
    public MapCodec<? extends CommandBuilderNode> codec() {
        return CODEC;
    }
}
