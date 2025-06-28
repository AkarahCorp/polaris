package dev.akarah.cdata.registry.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.registry.text.TextElement;
import dev.akarah.cdata.script.action.ActionProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record CustomEntity(
    EntityType<?> entityType,
    Optional<Holder<TextElement>> nameTemplate,
    Optional<Holder<TextElement>> infoTemplate,
    Optional<StatsObject> stats,
    Optional<ActionProvider> onTick
) {
    public static Codec<CustomEntity> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EntityType.CODEC.fieldOf("type").forGetter(CustomEntity::entityType),
            TextElement.HOLDER_CODEC.optionalFieldOf("name_template").forGetter(CustomEntity::nameTemplate),
            TextElement.HOLDER_CODEC.optionalFieldOf("info_template").forGetter(CustomEntity::infoTemplate),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomEntity::stats),
            ActionProvider.CODEC.optionalFieldOf("on_tick").forGetter(CustomEntity::onTick)
    ).apply(instance, CustomEntity::new)));

    public DynamicEntity spawn(Level level, Vec3 position) {
        var entity = new DynamicEntity(EntityType.AXOLOTL, level, this);
        entity.teleportTo(position.x, position.y, position.z);
        level.addFreshEntity(entity);
        return entity;
    }
}
