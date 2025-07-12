package dev.akarah.cdata.mixin;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Shadow public ServerGamePacketListenerImpl connection;

    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    public void fixName(CallbackInfoReturnable<Component> cir) {
        if(((Object) this) instanceof FakePlayer fakePlayer) {
            cir.setReturnValue(fakePlayer.getCustomName());
        }
    }
}
