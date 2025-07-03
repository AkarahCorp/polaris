package dev.akarah.cdata.mixin;

import net.tslat.smartbrainlib.SBLFabric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SBLFabric.class)
public class SBLFabricMixin {
    @Inject(at = @At("HEAD"), method = "isDevEnv", cancellable = true, remap = false)
    public void returnFalse(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
