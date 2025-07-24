package dev.akarah.cdata.mixin;

import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.value.event.REntityDamageEvent;
import dev.akarah.cdata.script.value.mc.REntity;
import dev.akarah.cdata.script.value.RNumber;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    public void fixName(CallbackInfoReturnable<Component> cir) {
        if(((Object) this) instanceof FakePlayer fakePlayer) {
            cir.setReturnValue(fakePlayer.getCustomName());
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    public void damageEvent(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        var e = (ServerPlayer) (Object) this;
        if(!(e instanceof FakePlayer)) {
            var event = REntityDamageEvent.of(REntity.of(e), RNumber.of(f));
            var functions = Resources.actionManager().functionsByEventType("player.hurt");
            Resources.actionManager().callEvents(functions, event);
            if(event.cancelled) {
                cir.setReturnValue(false);
            }
        }
    }
}
