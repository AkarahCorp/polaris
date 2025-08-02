package dev.akarah.cdata.registry.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.mixin.CommandContextMixin;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.value.RBoolean;
import dev.akarah.cdata.script.value.RDict;
import dev.akarah.cdata.script.value.RNumber;
import dev.akarah.cdata.script.value.RString;
import dev.akarah.cdata.script.value.mc.REntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

public record ExecutesCommandNode(ResourceLocation action) implements CommandBuilderNode {
    public static MapCodec<ExecutesCommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("action").forGetter(ExecutesCommandNode::action)
    ).apply(instance, ExecutesCommandNode::new));

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node) {
        return node.executes(ctx -> {
            var obj = RDict.create();
            for(var argument : ((CommandContextMixin) ctx).arguments().entrySet()) {
                switch (argument.getValue().getResult()) {
                    case String value ->
                            obj.javaValue().put(RString.of(argument.getKey()), RString.of(value));
                    case Double value ->
                            obj.javaValue().put(RString.of(argument.getKey()), RNumber.of(value));
                    case Boolean value ->
                            obj.javaValue().put(RString.of(argument.getKey()), RBoolean.of(value));
                    default -> {}
                }
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
