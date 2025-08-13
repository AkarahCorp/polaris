package dev.akarah.cdata.registry.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.mixin.CommandContextMixin;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.value.*;
import dev.akarah.cdata.script.value.mc.REntity;
import dev.akarah.cdata.script.value.mc.RVector;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record ExecutesCommandNode(ResourceLocation action) implements CommandBuilderNode {
    public static MapCodec<ExecutesCommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("action").forGetter(ExecutesCommandNode::action)
    ).apply(instance, ExecutesCommandNode::new));

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> dispatch(ArgumentBuilder<CommandSourceStack, ?> node) {
        return node.executes(ctx -> {
            var obj = RDict.create();
            for(var argument : ((CommandContextMixin) ctx).arguments().entrySet()) {
                RDict.put(obj, RString.of(argument.getKey()), RuntimeValue.from(argument.getValue().getResult()));
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
