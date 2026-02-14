package dev.akarah.polaris.mixin;

import dev.akarah.polaris.registry.entity.instance.DynamicEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(at = @At("HEAD"), method = "shouldBeSaved", cancellable = true)
    public void shouldBeSaved(CallbackInfoReturnable<Boolean> cir) {
        var e = (Entity) (Object) this;
        var dyn = DynamicEntity.castDynOwner(e);
        if(dyn != e) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
