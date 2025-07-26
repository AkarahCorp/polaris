package dev.akarah.cdata.mixin;

import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.value.mc.REntity;
import dev.akarah.cdata.script.value.mc.RVector;
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
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;setPlacedBy(" +
                            "Lnet/minecraft/world/level/Level;" +
                            "Lnet/minecraft/core/BlockPos;" +
                            "Lnet/minecraft/world/level/block/state/BlockState;" +
                            "Lnet/minecraft/world/entity/LivingEntity;" +
                            "Lnet/minecraft/world/item/ItemStack;" +
                            ")V"
            ),
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
            blockPlaceContext.getPlayer().containerMenu.setCarried(blockPlaceContext.getItemInHand());
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
