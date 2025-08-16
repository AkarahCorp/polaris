package dev.akarah.polaris.mixin;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.mc.REntity;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void playerJoinEvent(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        Resources.actionManager().performEvents("player.join", REntity.of(serverPlayer));
        Resources.statManager().refreshPlayerInventories();
    }

    @Inject(method = "remove", at = @At("TAIL"))
    public void playerQuitEvent(ServerPlayer serverPlayer, CallbackInfo ci) {
        Resources.actionManager().performEvents("player.quit", REntity.of(serverPlayer));

        REntity.scoreboards.remove(serverPlayer.getUUID());
        REntity.objectives.remove(serverPlayer.getUUID());
    }
}
