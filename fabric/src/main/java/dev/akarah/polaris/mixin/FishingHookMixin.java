package dev.akarah.polaris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.akarah.polaris.registry.Resources;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Shadow private int timeUntilLured;

    @Shadow private int timeUntilHooked;

    @Shadow @Nullable public abstract Player getPlayerOwner();

    @Shadow
    public int nibble;

    // disable fishing speed change when underwater
    @WrapOperation(
            method = "catchingFish",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;canSeeSky(Lnet/minecraft/core/BlockPos;)Z")
    )
    public boolean wrap(Level instance, BlockPos blockPos, Operation<Boolean> original) {
        return true;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if(this.nibble <= 0 && this.timeUntilLured <= 0) {
            Resources.config().fishingConfig().ifPresent(fishingConfig -> {
                var lureStat = Resources.statManager().lookup((ServerPlayer) this.getPlayerOwner()).get(fishingConfig.lureSpeedStat());
                this.timeUntilLured = Math.max(fishingConfig.baseLureTime() - (int) lureStat, 5);
            });
        }
        if(this.timeUntilHooked <= 0 && this.timeUntilLured == 1) {
            Resources.config().fishingConfig().ifPresent(fishingConfig -> {
                var hookStat = Resources.statManager().lookup((ServerPlayer) this.getPlayerOwner()).get(fishingConfig.hookSpeedStat());
                this.timeUntilHooked = Math.max(fishingConfig.baseHookTime() - (int) hookStat, 5);
            });
        }
    }
}
