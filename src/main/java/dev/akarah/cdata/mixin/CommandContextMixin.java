package dev.akarah.cdata.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CommandContext.class)
public interface CommandContextMixin {
    @Accessor(value = "arguments")
    Map<String, ParsedArgument<?, ?>> arguments();
}
