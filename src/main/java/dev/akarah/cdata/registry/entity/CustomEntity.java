package dev.akarah.cdata.registry.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.behavior.ActivityMap;
import dev.akarah.cdata.registry.stat.StatsObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record CustomEntity(
        EntityType<?> entityType,
        String name,
        Optional<StatsObject> stats,
        ActivityMap brain,
        Optional<EntityEvents> events

) {
    public static Codec<CustomEntity> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EntityType.CODEC.fieldOf("type").forGetter(CustomEntity::entityType),
            Codec.STRING.fieldOf("name").forGetter(CustomEntity::name),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomEntity::stats),
            ActivityMap.CODEC.optionalFieldOf("brain", new ActivityMap(Map.of())).forGetter(CustomEntity::brain),
            EntityEvents.CODEC.optionalFieldOf("events").forGetter(CustomEntity::events)
    ).apply(instance, CustomEntity::new)));

    public DynamicEntity spawn(Level level, Vec3 position) {
        var entity = DynamicEntity.create(EntityType.ZOMBIE, level, this);
        entity.teleportTo(position.x, position.y, position.z);
        level.addFreshEntity(entity);
        return entity;
    }
}
