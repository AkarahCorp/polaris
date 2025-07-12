package dev.akarah.cdata.registry.stat;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.registry.entity.CustomEntity;
import dev.akarah.cdata.registry.item.CustomItem;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

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
        var eventName = ResourceLocation.withDefaultNamespace("player/tick");
        var functions = ExtReloadableResources.actionManager().functionsByEvent(eventName);

        for(var player : Main.server().getPlayerList().getPlayers()) {
            var stats = StatsObject.of();
            stats.add(ExtReloadableResources.config().baseStats());
            for(var slot : LOOPED_SLOTS) {
                var item = player.getItemBySlot(slot);
                CustomItem.itemOf(item).ifPresent(customItem -> {
                    customItem.equippable().ifPresent(equippableData -> {
                        if(slot.equals(equippableData.slot())) {
                            stats.add(customItem.stats().orElse(StatsObject.EMPTY));
                        }
                    });
                });
            }
            this.set(player, stats.performFinalCalculations());

            ExtReloadableResources.actionManager().callFunctions(functions, List.of(player));

            var packet = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                    new HashSet<>(CustomEntity.FAKE_PLAYERS)
            );
            player.connection.send(packet);
        }

        ExtReloadableResources.mobSpawnRule().registry().listElements().forEach(rule -> rule.value().tick());
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
