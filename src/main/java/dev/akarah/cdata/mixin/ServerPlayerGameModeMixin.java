package dev.akarah.cdata.mixin;

import com.google.common.util.concurrent.AtomicDouble;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.entity.EntityUtil;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.item.ItemEvents;
import dev.akarah.cdata.registry.mining.MiningManager;
import dev.akarah.cdata.script.value.REntity;
import dev.akarah.cdata.script.value.RItem;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
                    this.level.getBlockState(blockPos).getBlock(),
                    blockPos
            );
            rule.ifPresent(miningRule -> {
                Resources.miningManager().setStatus(
                        this.player,
                        new MiningManager.MiningStatus(
                                blockPos,
                                miningRule,
                                new AtomicDouble(0.0)
                        )
                );
            });
        }
        if(action.equals(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK)) {
            Resources.miningManager().clearStatus(this.player);
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"))
    public void useItem(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        for(var item : EntityUtil.equipmentItemsOf(serverPlayer)) {
            CustomItem.itemOf(item).flatMap(CustomItem::events).flatMap(ItemEvents::onRightClick)
                    .ifPresent(events -> Resources.actionManager().callFunctions(
                            events,
                            List.of(REntity.of(serverPlayer), RItem.of(item))
                    ));
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    public void useItem(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        for(var item : EntityUtil.equipmentItemsOf(serverPlayer)) {
            CustomItem.itemOf(item).flatMap(CustomItem::events).flatMap(ItemEvents::onRightClick)
                    .ifPresent(events -> Resources.actionManager().callFunctions(
                            events,
                            List.of(REntity.of(serverPlayer), RItem.of(item))
                    ));
        }
    }
}
