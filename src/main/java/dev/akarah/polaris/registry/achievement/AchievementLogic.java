package dev.akarah.polaris.registry.achievement;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RIdentifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AchievementLogic {
    public static void activate(ServerPlayer player, ResourceLocation location) {
        for(var ach : Resources.ACHIEVEMENT_KIND.registry().entrySet()) {
            if(ach.getValue().triggers().contains(location) && ach.getValue().criteria().evaluate(player)) {
                try {
                    Resources.actionManager().performEvents("player.complete_achievement", REntity.of(player), RIdentifier.of(ach.getKey().location()));
                } catch (Throwable e) {

                }
            }
        }
    }
}
