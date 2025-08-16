package dev.akarah.polaris.mixin;

import com.google.common.util.concurrent.AtomicDouble;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.entity.EntityUtil;
import dev.akarah.polaris.registry.mining.MiningManager;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RItem;
import dev.akarah.polaris.script.value.mc.RVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow @Final protected ServerPlayer player;

    @Shadow protected ServerLevel level;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    public void handleBlockBreakAction(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
        if(action.equals(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK)
            && this.player.gameMode().equals(GameType.SURVIVAL)) {
            Resources.miningManager().clearStatus(this.player);
            var rule = Resources.miningManager().ruleForMaterial(
                    this.level.getBlockState(blockPos),
                    blockPos
            );
            rule.ifPresent(miningRule -> Resources.miningManager().setStatus(
                    this.player,
                    new MiningManager.MiningStatus(
                            blockPos,
                            miningRule,
                            new AtomicDouble(0.0)
                    )
            ));
        }
        if(action.equals(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK)) {
            Resources.miningManager().clearStatus(this.player);
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"))
    public void useItem(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        for(var item : EntityUtil.equipmentItemsOf(serverPlayer)) {
            Resources.actionManager().performEvents(
                    "item.right_click",
                    REntity.of(serverPlayer),
                    RItem.of(item)
            );
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void useItemOn(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        var result = Resources.actionManager().performEvents(
                "player.right_click_block",
                REntity.of(serverPlayer),
                RVector.of(blockHitResult.getBlockPos().getCenter())
        );
        if(!result) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            cir.cancel();
        }
        for(var item : EntityUtil.equipmentItemsOf(serverPlayer)) {
            Resources.actionManager().performEvents(
                    "item.right_click",
                    REntity.of(serverPlayer),
                    RItem.of(item)
            );
        }
    }
}
