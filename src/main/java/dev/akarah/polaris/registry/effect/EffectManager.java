package dev.akarah.polaris.registry.effect;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
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
        // TODO: finish
        return false;
    };
}
