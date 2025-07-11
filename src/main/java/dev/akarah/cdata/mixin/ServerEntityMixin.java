package dev.akarah.cdata.mixin;

import dev.akarah.cdata.registry.entity.CustomEntity;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.HashSet;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
    @Inject(method = "addPairing", at = @At("HEAD"))
    public void addPairing(ServerPlayer serverPlayer, CallbackInfo ci) {
        var packet = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(
                        ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
                ),
                new HashSet<>(CustomEntity.FAKE_PLAYERS)
        );
        serverPlayer.connection.send(packet);
    }
}
