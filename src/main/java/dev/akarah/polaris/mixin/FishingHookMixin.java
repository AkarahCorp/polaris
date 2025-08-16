package dev.akarah.polaris.mixin;

import dev.akarah.polaris.registry.Resources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
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

    @Shadow private int nibble;

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
