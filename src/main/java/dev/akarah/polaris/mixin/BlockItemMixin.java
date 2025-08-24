package dev.akarah.polaris.mixin;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RVector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(
            method = "place",
            at = @At("HEAD"),
            cancellable = true
    )
    public void placeEvent(BlockPlaceContext blockPlaceContext, CallbackInfoReturnable<InteractionResult> cir) {
        if(blockPlaceContext.getPlayer() == null) {
            return;
        }
        var result = Resources.actionManager().performEvents(
                "player.place_block",
                REntity.of(blockPlaceContext.getPlayer()),
                RVector.of(blockPlaceContext.getClickLocation())
        );
        if(!result) {
            var player = blockPlaceContext.getPlayer();
            if(blockPlaceContext.getHand() == InteractionHand.MAIN_HAND) {
                player.getInventory().setSelectedItem(blockPlaceContext.getItemInHand());
                player.getInventory().setChanged();
                ((ServerPlayer) player).connection.send(
                        player.getInventory().createInventoryUpdatePacket(player.getInventory().getSelectedSlot())
                );
            } else {
                player.getInventory().setItem(40, blockPlaceContext.getItemInHand());
                player.getInventory().setChanged();
                ((ServerPlayer) player).connection.send(
                        player.getInventory().createInventoryUpdatePacket(40)
                );
            }
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }
}
