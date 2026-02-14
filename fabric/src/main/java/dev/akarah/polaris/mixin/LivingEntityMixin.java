package dev.akarah.polaris.mixin;

import dev.akarah.polaris.registry.entity.instance.DynamicEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    public void pushEntities(CallbackInfo ci) {
        var e = (LivingEntity) (Object) this;
        var owner = DynamicEntity.castDynOwner(e);
        if(owner != e) {
            ci.cancel();
        }
    }

    @Inject(method = "getHitbox", at = @At("HEAD"), cancellable = true)
    public void getBox(CallbackInfoReturnable<AABB> cir) {
        var e = (Entity) (Object) this;
        var dyn = DynamicEntity.castDynOwner(e);
        if(dyn != e) {
            cir.setReturnValue(new AABB(0, 0, 0, 0, 0, 0));
            cir.cancel();
        }
    }

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    public void isPushable(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
        cir.cancel();
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    public void hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        var e = (Entity) (Object) this;
        var dyn = DynamicEntity.castDynOwner(e);
        if(dyn != e) {
            cir.setReturnValue(true);
            cir.cancel();
            dyn.hurtServer(serverLevel, damageSource, f);
        }
   }

}
