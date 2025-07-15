package dev.akarah.cdata.mixin;

import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.value.REntity;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void playerJoinEvent(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        var eventName = ResourceLocation.withDefaultNamespace("player/join");
        var functions = Resources.actionManager().functionsByEvent(eventName);
        Resources.actionManager().callFunctions(functions, List.of(REntity.of(serverPlayer)));
    }

    @Inject(method = "remove", at = @At("TAIL"))
    public void playerQuitEvent(ServerPlayer serverPlayer, CallbackInfo ci) {
        var eventName = ResourceLocation.withDefaultNamespace("player/quit");
        var functions = Resources.actionManager().functionsByEvent(eventName);
        Resources.actionManager().callFunctions(functions, List.of(REntity.of(serverPlayer)));
    }
}
