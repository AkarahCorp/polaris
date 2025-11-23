package dev.akarah.polaris.registry.stat;

import dev.akarah.polaris.Main;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.achievement.AchievementLogic;
import dev.akarah.polaris.registry.entity.CustomEntity;
import dev.akarah.polaris.registry.item.CustomItem;
import dev.akarah.polaris.registry.item.value.CustomItemComponents;
import dev.akarah.polaris.script.value.RNullable;
import dev.akarah.polaris.script.value.RStatsObject;
import dev.akarah.polaris.script.value.mc.REntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.*;
import java.util.List;

public class StatManager {
    private final Map<UUID, StatsObject> playerStats = new HashMap<>();

    public StatsObject lookup(ServerPlayer serverPlayer) {
        return this.playerStats.getOrDefault(serverPlayer.getUUID(), StatsObject.of());
    }

    public StatsObject lookup(UUID uuid) {
        return this.playerStats.getOrDefault(uuid, StatsObject.of());
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

    public void tickPlayers() {

        for(var player : Main.server().getPlayerList().getPlayers()) {
            var sources = StatsObject.of();

            Resources.statType().registry().entrySet().forEach(entry -> {
                sources.add(new StatsObject.SourceEntry(
                        Component.literal("Base Stat"),
                        entry.getKey().location(),
                        StatsObject.SourceOperation.ADD,
                        entry.getValue().baseValue()
                ));
            });
            for(var slot : LOOPED_SLOTS) {
                var item = player.getItemBySlot(slot);
                CustomItem.itemOf(item).ifPresent(customItem -> customItem.components().flatMap(CustomItemComponents::equippable).ifPresent(equippableData -> {
                    if(slot.equals(equippableData.slot())) {
                        var addedStats = customItem.modifiedStats(RNullable.of(REntity.of(player)), item.copy());
                        sources.add(addedStats);
                    }
                }));
            }

            sources.add(Resources.effectManager().getPlayerStats(player));

            Resources.actionManager().performEvents("player.stat_tick", REntity.of(player), RStatsObject.of(sources));

            this.set(player, sources.performFinalCalculations());
            Resources.actionManager().performEvents("player.tick", REntity.of(player));
            AchievementLogic.activate(player, ResourceLocation.fromNamespaceAndPath("polaris", "player.tick"));


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
    }

    public void refreshPlayerInventories() {
        try {
            for(var player : Main.server().getPlayerList().getPlayers()) {
                refreshPlayerInventory(player);
            }
        } catch (NullPointerException ignored) {

        }
    }

    public void refreshPlayerInventory(ServerPlayer player) {
        try {
            for(int slot = 0; slot < 40; slot++) {
                var item = player.getInventory().getItem(slot);
                var customData = item.get(DataComponents.CUSTOM_DATA);

                int finalSlot = slot;
                CustomItem.itemOf(item).ifPresent(customItem -> {
                    var amount = item.getCount();

                    var newItem = customItem.toItemStack(RNullable.of(REntity.of(player)), customData, amount);
                    newItem.setCount(amount);

                    newItem.set(DataComponents.CUSTOM_DATA, customData);

                    player.getInventory().setItem(finalSlot, newItem);
                });
            }
        } catch (NullPointerException exception) {

        }
    }
}
