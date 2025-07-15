package dev.akarah.cdata.mixin;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import dev.akarah.cdata.registry.entity.EntityEvents;
import dev.akarah.cdata.registry.entity.EntityUtil;
import dev.akarah.cdata.registry.entity.VisualEntity;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.item.ItemEvents;
import dev.akarah.cdata.script.value.REntity;
import dev.akarah.cdata.script.value.RItem;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {
    @Shadow public ServerPlayer player;

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
                if(interactionHand.equals(InteractionHand.MAIN_HAND)) {
                    if(target instanceof VisualEntity visual) {
                        var events = visual.dynamic().base().events().flatMap(EntityEvents::onInteract).orElse(List.of());
                        Resources.actionManager().callFunctions(events, List.of(REntity.of(player), REntity.of(visual.dynamic())));
                    } else if(target instanceof DynamicEntity dynamicEntity) {
                        var events = dynamicEntity.base().events().flatMap(EntityEvents::onInteract).orElse(List.of());
                        Resources.actionManager().callFunctions(events, List.of(REntity.of(player), REntity.of(dynamicEntity)));
                    }
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
            CustomItem.itemOf(item).flatMap(CustomItem::events).flatMap(ItemEvents::onLeftClick)
                    .ifPresent(events -> Resources.actionManager().callFunctions(
                            events,
                            List.of(REntity.of(this.player), RItem.of(item))
                    ));
        }
    }
}
