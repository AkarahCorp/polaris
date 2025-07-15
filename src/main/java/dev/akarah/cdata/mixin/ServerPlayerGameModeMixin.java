package dev.akarah.cdata.mixin;

import com.google.common.util.concurrent.AtomicDouble;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.mining.MiningManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow @Final protected ServerPlayer player;

    @Shadow protected ServerLevel level;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    public void handleBlockBreakAction(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
        System.out.println(action);
        if(action.equals(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK)
            && this.player.gameMode().equals(GameType.SURVIVAL)) {
            Resources.miningManager().clearStatus(this.player);
            var rule = Resources.miningManager().ruleForMaterial(
                    this.level.getBlockState(blockPos).getBlock(),
                    blockPos
            );
            System.out.println(rule);
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
}
