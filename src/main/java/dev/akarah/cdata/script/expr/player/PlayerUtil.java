package dev.akarah.cdata.script.expr.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PlayerUtil {
    public static void sendSystemMessage(Entity entity, Component component, boolean overlay) {
        if(entity instanceof ServerPlayer player) {
            player.sendSystemMessage(component, overlay);
        }
    }
}
