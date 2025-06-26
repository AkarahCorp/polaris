package dev.akarah.cdata.registry.stat;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.property.Properties;
import dev.akarah.cdata.property.Property;
import dev.akarah.cdata.registry.citem.CustomItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StatManager {
    private final Map<UUID, StatsObject> playerStats = new HashMap<>();

    public StatsObject lookup(ServerPlayer serverPlayer) {
        return this.playerStats.getOrDefault(serverPlayer.getUUID(), StatsObject.EMPTY);
    }

    public StatsObject lookup(UUID uuid) {
        return this.playerStats.getOrDefault(uuid, StatsObject.EMPTY);
    }

    public void set(ServerPlayer serverPlayer, StatsObject statsObject) {
        this.playerStats.put(serverPlayer.getUUID(), statsObject);
    }

    public void set(UUID uuid, StatsObject statsObject) {
        this.playerStats.put(uuid, statsObject);
    }

    static List<EquipmentSlot> LOOPED_SLOTS = List.of(
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND
    );

    public void loopPlayers() {
        for(var player : Main.server().getPlayerList().getPlayers()) {
            var stats = StatsObject.of();
            for(var slot : LOOPED_SLOTS) {
                var item = player.getItemBySlot(slot);
                CustomItem.itemOf(item).ifPresent(customItem -> {
                    customItem.properties().get(Properties.EQUIPPABLE).ifPresent(equippableData -> {
                        if(slot.equals(equippableData.slot())) {
                            stats.add(customItem.properties().get(Properties.STATS).orElse(StatsObject.EMPTY));
                        }
                    });
                });
            }
            this.set(player, stats.performFinalCalculations());
        }
    }
}
