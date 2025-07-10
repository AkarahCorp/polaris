package dev.akarah.cdata.registry.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record EntityEvents(
        Optional<List<ResourceLocation>> onPlayerAttack,
        Optional<List<ResourceLocation>> onTakeDamage,
        Optional<List<ResourceLocation>> onPlayerKill,
        Optional<List<ResourceLocation>> onDeath,
        Optional<List<ResourceLocation>> onTick
) {
    public static Codec<EntityEvents> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("player_attack").forGetter(EntityEvents::onPlayerAttack),
            ResourceLocation.CODEC.listOf().optionalFieldOf("take_damage").forGetter(EntityEvents::onTakeDamage),
            ResourceLocation.CODEC.listOf().optionalFieldOf("player_kill").forGetter(EntityEvents::onPlayerKill),
            ResourceLocation.CODEC.listOf().optionalFieldOf("death").forGetter(EntityEvents::onDeath),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tick").forGetter(EntityEvents::onTick)
    ).apply(instance, EntityEvents::new)));
}
