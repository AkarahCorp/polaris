package dev.akarah.cdata.mixin;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.value.RCell;
import dev.akarah.cdata.script.value.mc.REntity;
import dev.akarah.cdata.script.value.RNumber;
import dev.akarah.cdata.script.value.mc.RIdentifier;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.registries.Registries;
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

    @Inject(
            method = "hurtServer",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/player/Player;" +
                    "hurtServer(" +
                    "Lnet/minecraft/server/level/ServerLevel;" +
                    "Lnet/minecraft/world/damagesource/DamageSource;" +
                    "F" +
                    ")Z"
            ),
            cancellable = true
    )
    public void damageEvent(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        var e = (ServerPlayer) (Object) this;
        if(!(e instanceof FakePlayer)) {
            var cell = RCell.create(RNumber.of(f));

            var id = Main.server().registryAccess().lookup(Registries.DAMAGE_TYPE).orElseThrow()
                    .getKey(damageSource.type());

            var result = Resources.actionManager().performEvents(
                    "player.hurt",
                    REntity.of(e), cell, RIdentifier.of(id)
            );
            if(!result) {
                cir.setReturnValue(true);
            }
            f = ((Double) (cell.javaValue())).floatValue();
        }
    }
}
