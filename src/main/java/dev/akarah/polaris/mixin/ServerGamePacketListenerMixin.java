package dev.akarah.polaris.mixin;

import dev.akarah.polaris.building.wand.WandOperations;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.entity.instance.DynamicEntity;
import dev.akarah.polaris.registry.entity.EntityUtil;
import dev.akarah.polaris.script.value.RString;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    public void handlePlayerCommand(ServerboundPlayerActionPacket serverboundPlayerActionPacket, CallbackInfo ci) {
        switch (serverboundPlayerActionPacket.getAction()) {
            case SWAP_ITEM_WITH_OFFHAND -> {
                ci.cancel();
                Resources.actionManager().performEvents(
                        "player.swap_hands",
                        REntity.of(player)
                );
            }
        }
    }

    @Inject(method = "handleInteract", at = @At("TAIL"))
    public void interactEvent(ServerboundInteractPacket serverboundInteractPacket, CallbackInfo ci) {
        var target = serverboundInteractPacket.getTarget(this.player.level());
        if(serverboundInteractPacket.isUsingSecondaryAction()) {
            return;
        }
        var player = this.player;
        serverboundInteractPacket.dispatch(new ServerboundInteractPacket.Handler() {
            @Override
            public void onInteraction(InteractionHand interactionHand) {
                if(target == null) {
                    return;
                }
                if(interactionHand.equals(InteractionHand.MAIN_HAND)) {
                    Resources.actionManager().performEvents(
                            "entity.interact",
                            REntity.of(player), REntity.of(DynamicEntity.castDynOwner(target))
                    );
                }
            }

            @Override
            public void onInteraction(InteractionHand interactionHand, Vec3 vec3) {

            }

            @Override
            public void onAttack() {

            }
        });

    }

    @Inject(method = "handleAnimate", at = @At("TAIL"))
    public void handleLeftClick(ServerboundSwingPacket serverboundSwingPacket, CallbackInfo ci) {
        for(var item : EntityUtil.equipmentItemsOf(this.player)) {
            Resources.actionManager().performEvents(
                    "item.left_click",
                    REntity.of(this.player),
                    RItem.of(item)
            );
        }
        WandOperations.useWandLeftClick(this.player);
    }

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    public void chat(ServerboundChatPacket serverboundChatPacket, CallbackInfo ci) {
        var result = Resources.actionManager().performEvents(
                "player.send_chat_message",
                REntity.of(this.player),
                RString.of(serverboundChatPacket.message())
        );
        if(!result) {
            ci.cancel();
        }
    }

    @Inject(method = "handleContainerClick", at = @At("TAIL"))
    public void click(ServerboundContainerClickPacket serverboundContainerClickPacket, CallbackInfo ci) {
        Resources.statManager().refreshPlayerInventory(this.player);
    }

    @Inject(method = "handleContainerClose", at = @At("TAIL"))
    public void close(ServerboundContainerClosePacket serverboundContainerClosePacket, CallbackInfo ci) {
        Resources.statManager().refreshPlayerInventory(this.player);
    }

    @Inject(method = "handleChatCommand", at = @At("HEAD"), cancellable = true)
    public void command(ServerboundChatCommandPacket serverboundChatCommandPacket, CallbackInfo ci) {
        if(this.player.hasContainerOpen()) {
            this.player.sendSystemMessage(Component.literal("You can not execute commands while you have a container open!"));

            ci.cancel();
        }
    }
}
