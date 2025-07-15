package dev.akarah.cdata.registry.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.entity.behavior.PrioritizedTask;
import dev.akarah.cdata.registry.stat.StatsObject;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public record CustomEntity(
        EntityType<?> entityType,
        String name,
        Optional<StatsObject> stats,
        Optional<List<PrioritizedTask>> behaviorGoals,
        Optional<List<PrioritizedTask>> targetGoals,
        Optional<EntityEvents> events,
        Map<EquipmentSlot, ResourceLocation> equipment,
        boolean invulnerable,
        UUID playerSkinName
) {
    public static List<FakePlayer> FAKE_PLAYERS = Lists.newArrayList();
    public static Map<UUID, GameProfile> GAME_PROFILES = Maps.newHashMap();

    public static Codec<CustomEntity> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EntityType.CODEC.fieldOf("type").forGetter(CustomEntity::entityType),
            Codec.STRING.fieldOf("name").forGetter(CustomEntity::name),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomEntity::stats),
            PrioritizedTask.CODEC.listOf().optionalFieldOf("behavior_goals").forGetter(CustomEntity::behaviorGoals),
            PrioritizedTask.CODEC.listOf().optionalFieldOf("target_goals").forGetter(CustomEntity::targetGoals),
            EntityEvents.CODEC.optionalFieldOf("events").forGetter(CustomEntity::events),
            Codec.unboundedMap(EquipmentSlot.CODEC, ResourceLocation.CODEC).optionalFieldOf("equipment", Map.of()).forGetter(CustomEntity::equipment),
            Codec.BOOL.optionalFieldOf("invulnerable", false).forGetter(CustomEntity::invulnerable),
            UUIDUtil.CODEC.optionalFieldOf("player_skin", UUID.fromString("092f1802-e1ad-4f3a-a129-da32eb2227de")).forGetter(CustomEntity::playerSkinName)
    ).apply(instance, CustomEntity::new)));

    public ResourceLocation id() {
        return Resources.customEntity()
                .registry()
                .getKey(this);
    }

    public DynamicEntity spawn(Level level, Vec3 position) {
        var entity = DynamicEntity.create(EntityType.ZOMBIE, level, this);
        entity.setCustomNameVisible(false);
        entity.teleportTo(position.x, position.y, position.z);
        level.addFreshEntity(entity);

        entity.visual.teleportTo(position.x, position.y, position.z);
        level.addFreshEntity(entity.visual);
        level.addFreshEntity(entity.visual.fakeName);
        return entity;
    }
}
