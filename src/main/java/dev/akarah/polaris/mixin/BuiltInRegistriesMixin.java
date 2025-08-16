package dev.akarah.polaris.mixin;

import dev.akarah.polaris.registry.ExtBuiltInRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void initExtRegistries(CallbackInfo ci) {
        ExtBuiltInRegistries.bootStrap();
    }
}
