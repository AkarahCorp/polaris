package dev.akarah.cdata.mixin;

import dev.akarah.cdata.property.Properties;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.ExtRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesMixin {
    @Shadow
    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, BuiltInRegistries.RegistryBootstrap<T> registryBootstrap) {
        return null;
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void initExtRegistries(CallbackInfo ci) {
        ExtBuiltInRegistries.PROPERTIES = registerSimple(ExtRegistries.PROPERTIES, (registry) -> Properties.NAME);
    }
}
