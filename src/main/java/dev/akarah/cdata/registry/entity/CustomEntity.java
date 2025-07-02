package dev.akarah.cdata.registry.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.behavior.Behavior;
import dev.akarah.cdata.registry.entity.behavior.PrioritizableBehavior;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.registry.text.TextElement;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record CustomEntity(
        EntityType<?> entityType,
        Optional<TextElement> nameTemplate,
        Optional<TextElement> infoTemplate,
        Optional<StatsObject> stats,
        List<PrioritizableBehavior> behaviors
) {
    public static Codec<CustomEntity> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EntityType.CODEC.fieldOf("type").forGetter(CustomEntity::entityType),
            TextElement.CODEC_BY_ID.optionalFieldOf("name_template").forGetter(CustomEntity::nameTemplate),
            TextElement.CODEC_BY_ID.optionalFieldOf("info_template").forGetter(CustomEntity::infoTemplate),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomEntity::stats),
            PrioritizableBehavior.CODEC.listOf().fieldOf("behaviors").forGetter(CustomEntity::behaviors)
    ).apply(instance, CustomEntity::new)));

    public DynamicEntity spawn(Level level, Vec3 position) {
        var entity = DynamicEntity.create(EntityType.AXOLOTL, level, this);
        entity.teleportTo(position.x, position.y, position.z);
        level.addFreshEntity(entity);
        return entity;
    }
}
