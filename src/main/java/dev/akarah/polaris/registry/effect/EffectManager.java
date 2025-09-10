package dev.akarah.polaris.registry.effect;

import dev.akarah.polaris.Main;
import dev.akarah.polaris.registry.stat.StatsObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EffectManager {
    private final Map<UUID, Map<ResourceLocation, EffectObject>> playerEffects = new HashMap<>();

    public boolean tryAddEffect(UUID uuid, ResourceLocation identifier, EffectObject effect) {
        playerEffects.putIfAbsent(uuid, new HashMap<>());
        var playerEffect = playerEffects.get(uuid);
        if (!playerEffect.containsKey(identifier)){
            playerEffect.put(identifier, effect);
            return true;
        }
        var predefinedEffect = playerEffect.get(identifier);
        if (predefinedEffect.level < effect.level || (predefinedEffect.level == effect.level && predefinedEffect.durationRemainingTicks < effect.durationRemainingTicks)) {
            playerEffect.put(identifier, effect);
            return true;
        }
        return false;
    }

    public boolean tryAddEffect(Player player, ResourceLocation identifier, EffectObject effect) {
        return tryAddEffect(player.getUUID(), identifier, effect);
    }

    public Map<ResourceLocation, EffectObject> getPlayerEffects(UUID uuid) {
        return playerEffects.getOrDefault(uuid, new HashMap<>());
    }

    public Map<ResourceLocation, EffectObject> getPlayerEffects(Player player) {
        return getPlayerEffects(player.getUUID());
    }

    public StatsObject getPlayerStats(UUID uuid) {
        var stats = StatsObject.EMPTY;
        for (Map.Entry<ResourceLocation, EffectObject> entry : getPlayerEffects(uuid).entrySet()) {
            stats.add(entry.getValue().getStats());
        }
        return stats;
    }

    public StatsObject getPlayerStats(Player player) {
        return getPlayerStats(player.getUUID());
    }

    public void tickPlayers(){
        for (Player player : Main.server().getPlayerList().getPlayers()) {
            var iterator = playerEffects.get(player.getUUID()).entrySet().iterator();
            while (iterator.hasNext()) {
                EffectObject effect = iterator.next().getValue();
                effect.tick(player);
                if (effect.durationRemainingTicks <= 0) {iterator.remove();}
            }
        }
    }
}
