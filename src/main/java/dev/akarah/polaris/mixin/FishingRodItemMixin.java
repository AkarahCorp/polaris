package dev.akarah.polaris.mixin;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.mc.REntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {
    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void cast(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        Resources.config().fishingConfig().ifPresent(fishingConfig -> {
            if(player.fishing != null) {
                var pos = player.fishing.position();
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.FISHING_BOBBER_RETRIEVE,
                        SoundSource.NEUTRAL,
                        1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
                );
                cir.setReturnValue(InteractionResult.SUCCESS);
                cir.cancel();

                System.out.println(player.fishing.nibble);
                if(player.fishing.nibble > 0) {
                    var entities = fishingConfig.lootTable().execute((ServerLevel) player.level(), pos, (ServerPlayer) player);
                    for(var entity : entities) {
                        entity.addDeltaMovement(
                                player.position().subtract(entity.position()).multiply(0.2, 0.2, 0.2).add(0, 0.1, 0)
                        );
                    }

                    Resources.actionManager().performEvents(
                            "player.fish",
                            REntity.of(player)
                    );
                }

                player.fishing.discard();
            }
        });
    }
}
