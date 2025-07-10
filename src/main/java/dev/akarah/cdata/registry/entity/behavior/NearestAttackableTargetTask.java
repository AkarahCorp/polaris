package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public record NearestAttackableTargetTask(EntityType<?> target) implements TaskType {
    public static MapCodec<NearestAttackableTargetTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityType.CODEC.fieldOf("target").forGetter(NearestAttackableTargetTask::target)
    ).apply(instance, NearestAttackableTargetTask::new));

    @Override
    public Goal build(DynamicEntity entity) {
        return new NearestAttackableTargetGoal<>(entity, LivingEntity.class, true, ((livingEntity, _) -> livingEntity.getType().equals(this.target)));
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
