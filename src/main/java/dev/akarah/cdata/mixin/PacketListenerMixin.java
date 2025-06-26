package dev.akarah.cdata.mixin;

import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.env.Selection;
import dev.akarah.cdata.script.event.EventCaller;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class PacketListenerMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleAcceptPlayerLoad", at = @At("HEAD"))
    public void playerLoad(ServerboundPlayerLoadedPacket serverboundPlayerLoadedPacket, CallbackInfo ci) {
        var ctx = ScriptContext.of(Selection.of(this.player));
        EventCaller.callEvent(
                ResourceLocation.withDefaultNamespace("event/player/load"),
                ctx
        );
    }

    @Inject(method = "handleAnimate", at = @At("HEAD"))
    public void swing(ServerboundSwingPacket serverboundSwingPacket, CallbackInfo ci) {
        var ctx = ScriptContext.of(Selection.of(this.player));
        EventCaller.callEvent(
                ResourceLocation.withDefaultNamespace("event/player/swing"),
                ctx
        );
    }

    @Inject(method = "handleChat", at = @At("HEAD"))
    public void chat(ServerboundChatPacket serverboundChatPacket, CallbackInfo ci) {
        var ctx = ScriptContext.of(Selection.of(this.player));
        EventCaller.callEvent(
                ResourceLocation.withDefaultNamespace("event/player/chat"),
                ctx
        );
    }

    @Inject(method = "handleClientCommand", at = @At("HEAD"))
    public void chat(ServerboundClientCommandPacket serverboundClientCommandPacket, CallbackInfo ci) {
        switch(serverboundClientCommandPacket.getAction()) {
            case REQUEST_STATS -> {
                var ctx = ScriptContext.of(Selection.of(this.player));
                EventCaller.callEvent(
                        ResourceLocation.withDefaultNamespace("event/player/request_stats"),
                        ctx
                );
            }
            case PERFORM_RESPAWN -> {
                var ctx = ScriptContext.of(Selection.of(this.player));
                EventCaller.callEvent(
                        ResourceLocation.withDefaultNamespace("event/player/respawn"),
                        ctx
                );
            }
        }
    }

    @Inject(method = "handleInteract", at = @At("HEAD"))
    public void interact(ServerboundInteractPacket serverboundInteractPacket, CallbackInfo ci) {
        var ctx = ScriptContext.of(Selection.of(this.player));
        EventCaller.callEvent(
                ResourceLocation.withDefaultNamespace("event/player/interact"),
                ctx
        );
    }

    @Inject(method = "handleClientTickEnd", at = @At("HEAD"))
    public void tickEnd(ServerboundClientTickEndPacket serverboundClientTickEndPacket, CallbackInfo ci) {
        var ctx = ScriptContext.of(Selection.of(this.player));
        EventCaller.callEvent(
                ResourceLocation.withDefaultNamespace("event/player/client_tick_end"),
                ctx
        );
    }
}
