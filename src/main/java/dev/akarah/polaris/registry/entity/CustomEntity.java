package dev.akarah.polaris.registry.entity;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.entity.behavior.PrioritizedTask;
import dev.akarah.polaris.registry.entity.instance.DynamicEntity;
import dev.akarah.polaris.registry.loot.LootTable;
import dev.akarah.polaris.registry.stat.StatsObject;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public record CustomEntity(
        EntityType<?> entityType,
        String name,
        Optional<StatsObject> stats,
        Optional<List<PrioritizedTask>> behaviorGoals,
        Optional<List<PrioritizedTask>> targetGoals,
        Map<EquipmentSlot, Identifier> equipment,
        boolean invulnerable,
        Optional<ResolvableProfile> profile,
        Optional<LootTable> lootTable,
        CustomData customData
) {
    public static List<FakePlayer> FAKE_PLAYERS = Lists.newArrayList();

    public static Codec<CustomEntity> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EntityType.CODEC.fieldOf("type").forGetter(CustomEntity::entityType),
            Codec.STRING.fieldOf("name").forGetter(CustomEntity::name),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomEntity::stats),
            PrioritizedTask.CODEC.listOf().optionalFieldOf("behavior_goals").forGetter(CustomEntity::behaviorGoals),
            PrioritizedTask.CODEC.listOf().optionalFieldOf("target_goals").forGetter(CustomEntity::targetGoals),
            Codec.unboundedMap(EquipmentSlot.CODEC, Identifier.CODEC).optionalFieldOf("equipment", Map.of()).forGetter(CustomEntity::equipment),
            Codec.BOOL.optionalFieldOf("invulnerable", false).forGetter(CustomEntity::invulnerable),
            ResolvableProfile.CODEC.optionalFieldOf("profile").forGetter(CustomEntity::profile),
            LootTable.CODEC.optionalFieldOf("loot_table").forGetter(CustomEntity::lootTable),
            CustomData.CODEC.optionalFieldOf("custom_data", CustomData.EMPTY).forGetter(CustomEntity::customData)
    ).apply(instance, CustomEntity::new)));

    public Identifier id() {
        return Resources.customEntity()
                .registry()
                .getKey(this);
    }

    public DynamicEntity spawn(Level level, Vec3 position) {
        var entity = DynamicEntity.create(EntityType.ZOMBIE, level, this);
        entity.setCustomNameVisible(false);
        entity.teleportTo(position.x, position.y, position.z);
        level.addFreshEntity(entity);

        entity.wrappedEntity.teleportTo(position.x, position.y, position.z);
        level.addFreshEntity(entity.wrappedEntity);
        level.addFreshEntity(entity.fakeName);
        return entity;
    }
}
