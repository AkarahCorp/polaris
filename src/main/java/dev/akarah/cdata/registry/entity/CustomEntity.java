package dev.akarah.cdata.registry.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.registry.entity.behavior.PrioritizedTask;
import dev.akarah.cdata.registry.entity.behavior.TaskType;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.stat.StatsObject;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record CustomEntity(
        EntityType<?> entityType,
        String name,
        Optional<StatsObject> stats,
        Optional<List<PrioritizedTask>> behaviorGoals,
        Optional<List<PrioritizedTask>> targetGoals,
        Optional<EntityEvents> events,
        Map<EquipmentSlot, ResourceLocation> equipment

) {
    public static Codec<CustomEntity> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EntityType.CODEC.fieldOf("type").forGetter(CustomEntity::entityType),
            Codec.STRING.fieldOf("name").forGetter(CustomEntity::name),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomEntity::stats),
            PrioritizedTask.CODEC.listOf().optionalFieldOf("behavior_goals").forGetter(CustomEntity::behaviorGoals),
            PrioritizedTask.CODEC.listOf().optionalFieldOf("target_goals").forGetter(CustomEntity::targetGoals),
            EntityEvents.CODEC.optionalFieldOf("events").forGetter(CustomEntity::events),
            Codec.unboundedMap(EquipmentSlot.CODEC, ResourceLocation.CODEC).optionalFieldOf("equipment", Map.of()).forGetter(CustomEntity::equipment)
    ).apply(instance, CustomEntity::new)));

    public ResourceLocation id() {
        return ExtReloadableResources.customEntity()
                .registry()
                .getKey(this);
    }

    public DynamicEntity spawn(Level level, Vec3 position) {
        var entity = DynamicEntity.create(EntityType.ZOMBIE, level, this);
        entity.teleportTo(position.x, position.y, position.z);
        level.addFreshEntity(entity);
        return entity;
    }
}
