package dev.akarah.polaris.mixin;

import dev.akarah.polaris.registry.ExtBuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
    @Final
    @Shadow
    @Mutable
    public static List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void staticInit(CallbackInfo ci) {
        WORLDGEN_REGISTRIES = new ArrayList<>(WORLDGEN_REGISTRIES);
        WORLDGEN_REGISTRIES.addAll(ExtBuiltInRegistries.DYNAMIC_REGISTRIES);
        WORLDGEN_REGISTRIES = List.copyOf(WORLDGEN_REGISTRIES);
        System.out.println(
                WORLDGEN_REGISTRIES
                        .stream()
                        .map(RegistryDataLoader.RegistryData::key)
                        .toList()
        );
    }
}
