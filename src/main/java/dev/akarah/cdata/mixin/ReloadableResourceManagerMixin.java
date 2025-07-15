package dev.akarah.cdata.mixin;

import dev.akarah.cdata.registry.Resources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiPackResourceManager.class)
public abstract class ReloadableResourceManagerMixin implements ResourceManager {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void reloadLocalResources(PackType packType, List<?> list, CallbackInfo ci) {
        if(packType.equals(PackType.SERVER_DATA)) {
            Resources.reloadEverything(this);
        }
    }
}
