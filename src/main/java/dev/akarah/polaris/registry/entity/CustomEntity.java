package dev.akarah.polaris.registry.entity;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.entity.behavior.PrioritizedTask;
import dev.akarah.polaris.registry.stat.StatsObject;
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
        Map<EquipmentSlot, ResourceLocation> equipment,
        boolean invulnerable,
        Optional<UUID> playerSkinName
) {
    public static List<FakePlayer> FAKE_PLAYERS = Lists.newArrayList();

    public static Codec<CustomEntity> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EntityType.CODEC.fieldOf("type").forGetter(CustomEntity::entityType),
            Codec.STRING.fieldOf("name").forGetter(CustomEntity::name),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomEntity::stats),
            PrioritizedTask.CODEC.listOf().optionalFieldOf("behavior_goals").forGetter(CustomEntity::behaviorGoals),
            PrioritizedTask.CODEC.listOf().optionalFieldOf("target_goals").forGetter(CustomEntity::targetGoals),
            Codec.unboundedMap(EquipmentSlot.CODEC, ResourceLocation.CODEC).optionalFieldOf("equipment", Map.of()).forGetter(CustomEntity::equipment),
            Codec.BOOL.optionalFieldOf("invulnerable", false).forGetter(CustomEntity::invulnerable),
            UUIDUtil.CODEC.optionalFieldOf("player_skin").forGetter(CustomEntity::playerSkinName)
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
