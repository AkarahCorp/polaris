package dev.akarah.cdata.script.expr.player;

import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PlayerUtil {
    public static void bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/send_message"), PlayerSendMessageAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/send_actionbar"), PlayerSendActionbarAction.class);
    }

    public static void sendSystemMessage(Entity entity, Component component, boolean overlay) {
        if(entity instanceof ServerPlayer player) {
            player.sendSystemMessage(component, overlay);
        }
    }
}
