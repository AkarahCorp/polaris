package dev.akarah.cdata.registry.entity;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.ExtReloadableResources;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record MobSpawnRule(
        ResourceLocation entityType,
        ResourceKey<Level> level,
        Vec3 center,
        double radius,
        int limit
) {
    public static Codec<MobSpawnRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity_type").forGetter(MobSpawnRule::entityType),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(MobSpawnRule::level),
            Vec3.CODEC.fieldOf("center").forGetter(MobSpawnRule::center),
            Codec.DOUBLE.optionalFieldOf("radius", 10.0).forGetter(MobSpawnRule::radius),
            Codec.INT.optionalFieldOf("limit", 6).forGetter(MobSpawnRule::limit)
    ).apply(instance, MobSpawnRule::new));

    public void tick() {
        if(ExtReloadableResources.customEntity().registry().entrySet().isEmpty()) {
            return;
        }
        var level = Main.server().getLevel(this.level);
        assert level != null;

        var nearbyEntities = Streams.stream(level.getAllEntities())
                .toList()
                .stream()
                .filter(x -> x instanceof DynamicEntity)
                .map(x -> (DynamicEntity) x)
                .filter(dynamicEntity -> {
                    try {
                        return dynamicEntity.base().id().equals(this.entityType) && dynamicEntity.position().distanceTo(center) <= (radius * 2);
                    } catch (Exception e) {
                        dynamicEntity.remove(Entity.RemovalReason.KILLED);
                        return false;
                    }
                })
                .count();

        if (nearbyEntities < this.limit) {
            var pos = new BlockPos(
                    (int) (this.center.x + ((radius * 2) * (Math.random() - 0.5))),
                    (int) (this.center.y + radius),
                    (int) (this.center.z + ((radius * 2) * (Math.random() - 0.5)))
            );

            if(!level.isLoaded(pos)) {
                return;
            }


            while (level.isEmptyBlock(pos)) {
                pos = pos.below();
            }
            pos = pos.above();
            var entityBase = ExtReloadableResources.customEntity().registry().get(this.entityType).orElseThrow().value();
            entityBase.spawn(level, pos.getCenter());
        }
    }
}
