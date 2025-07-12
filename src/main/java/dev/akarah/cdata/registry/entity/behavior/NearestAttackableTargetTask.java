package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;

public record NearestAttackableTargetTask(ResourceLocation target) implements TaskType {
    public static MapCodec<NearestAttackableTargetTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("target").forGetter(NearestAttackableTargetTask::target)
    ).apply(instance, NearestAttackableTargetTask::new));

    @Override
    public Goal build(DynamicEntity entity) {
        throw new RuntimeException("please reimplement");
        // return new NearestAttackableTargetGoal<>(entity, LivingEntity.class, true, ((livingEntity, _) -> EntityUtil.entityType(livingEntity).equals(this.target)));
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
