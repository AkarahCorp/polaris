package dev.akarah.polaris.mixin;

import dev.akarah.polaris.Main;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.RuntimeValue;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RIdentifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {

    @Shadow
    @Final
    public Connection connection;

    @Inject(method = "handleCustomClickAction", at = @At("TAIL"))
    public void handleClickAction(ServerboundCustomClickActionPacket serverboundCustomClickActionPacket, CallbackInfo ci) {
        ServerPlayer player = null;
        for(var playerInst : Main.server().getPlayerList().getPlayers()) {
            if(playerInst.connection.connection.equals(this.connection)) {
                player = playerInst;
            }
        }

        if(player != null) {
            var value = RuntimeValue.CODEC.decode(NbtOps.INSTANCE, serverboundCustomClickActionPacket.payload().orElse(new CompoundTag()))
                    .getOrThrow().getFirst();
            Resources.actionManager().performEvents(
                    "player.custom_click_action",
                    REntity.of(player),
                    RIdentifier.of(serverboundCustomClickActionPacket.id()),
                    value
            );
        }
    }
}
