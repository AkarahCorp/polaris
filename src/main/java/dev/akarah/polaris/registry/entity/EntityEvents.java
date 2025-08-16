package dev.akarah.polaris.registry.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record EntityEvents(
        Optional<List<ResourceLocation>> onSpawn,
        Optional<List<ResourceLocation>> onPlayerAttack,
        Optional<List<ResourceLocation>> onTakeDamage,
        Optional<List<ResourceLocation>> onPlayerKill,
        Optional<List<ResourceLocation>> onDeath,
        Optional<List<ResourceLocation>> onTick,
        Optional<List<ResourceLocation>> onInteract
) {
    public static Codec<EntityEvents> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("spawn").forGetter(EntityEvents::onSpawn),
            ResourceLocation.CODEC.listOf().optionalFieldOf("player_attack").forGetter(EntityEvents::onPlayerAttack),
            ResourceLocation.CODEC.listOf().optionalFieldOf("take_damage").forGetter(EntityEvents::onTakeDamage),
            ResourceLocation.CODEC.listOf().optionalFieldOf("player_kill").forGetter(EntityEvents::onPlayerKill),
            ResourceLocation.CODEC.listOf().optionalFieldOf("death").forGetter(EntityEvents::onDeath),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tick").forGetter(EntityEvents::onTick),
            ResourceLocation.CODEC.listOf().optionalFieldOf("interact").forGetter(EntityEvents::onInteract)
    ).apply(instance, EntityEvents::new)));
}
