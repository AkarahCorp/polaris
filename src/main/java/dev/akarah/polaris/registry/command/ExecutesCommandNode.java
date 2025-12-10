package dev.akarah.polaris.registry.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.mixin.CommandContextMixin;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.*;
import dev.akarah.polaris.script.value.mc.REntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;

public record ExecutesCommandNode(Identifier action) implements CommandBuilderNode {
    public static MapCodec<ExecutesCommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("action").forGetter(ExecutesCommandNode::action)
    ).apply(instance, ExecutesCommandNode::new));

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node) {
        return node.executes(ctx -> {
            var obj = RDict.create();
            for(var argument : ((CommandContextMixin) ctx).arguments().entrySet()) {
                RDict.put(obj, RString.of(argument.getKey()), RuntimeValue.from(argument.getValue().getResult(), ctx.getSource()));
            }
            var returns = Resources.actionManager().executeBoolean(action, REntity.of(ctx.getSource().getEntityOrException()), obj);
            return returns ? 1 : 0;
        });
    }

    @Override
    public MapCodec<? extends CommandBuilderNode> codec() {
        return CODEC;
    }
}
