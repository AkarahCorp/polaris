package dev.akarah.cdata.registry.stat;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.entity.CustomEntity;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.item.value.CustomComponents;
import dev.akarah.cdata.script.value.RStatsObject;
import dev.akarah.cdata.script.value.mc.REntity;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.*;

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
            stats.add(Resources.config().baseStats());
            for(var slot : LOOPED_SLOTS) {
                var item = player.getItemBySlot(slot);
                StatsObject finalStats = stats;
                CustomItem.itemOf(item).ifPresent(customItem -> {
                    customItem.components().flatMap(CustomComponents::equippable).ifPresent(equippableData -> {
                        if(slot.equals(equippableData.slot())) {
                            finalStats.add(customItem.stats().orElse(StatsObject.EMPTY));
                        }
                    });
                });
            }

            Resources.statManager().set(player, stats);
            Resources.actionManager().performEvents("player.stat_tick", REntity.of(player), RStatsObject.of(stats));
            stats = Resources.statManager().lookup(player);
            this.set(player, stats.performFinalCalculations());
            Resources.actionManager().performEvents("player.tick", REntity.of(player));


            var packet = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                    new HashSet<>(CustomEntity.FAKE_PLAYERS)
            );
            player.connection.send(packet);

            var container = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
            if(container != null) {
                container.addOrReplacePermanentModifier(
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath("polaris", "overwrite_mining"),
                                -1000,
                                AttributeModifier.Operation.ADD_VALUE
                        )
                );
            }
        }

        Resources.mobSpawnRule().registry().listElements().forEach(rule -> rule.value().tick());
    }

    public void refreshPlayerInventories() {
        for(var player : Main.server().getPlayerList().getPlayers()) {
            for(int slot = 0; slot < 40; slot++) {
                var item = player.getInventory().getItem(slot);

                int finalSlot = slot;
                CustomItem.itemOf(item).ifPresent(customItem -> {
                    var amount = item.getCount();

                    var newItem = customItem.toItemStack();
                    newItem.setCount(amount);

                    player.getInventory().setItem(finalSlot, newItem);
                });
            }
        }
    }
}
