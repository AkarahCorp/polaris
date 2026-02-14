package dev.akarah.polaris.registry.entity.behavior;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.entity.instance.DynamicEntity;
import dev.akarah.polaris.script.value.mc.REntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public record NearestAttackableTargetTask(Identifier target) implements TaskType {
    public static MapCodec<NearestAttackableTargetTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("target").forGetter(NearestAttackableTargetTask::target)
    ).apply(instance, NearestAttackableTargetTask::new));

    @Override
    public Goal build(DynamicEntity entity) {
        return new NearestAttackableTargetGoal<>(
                entity,
                LivingEntity.class,
                true,
                ((livingEntity, _) -> REntity.type(REntity.of(livingEntity)).javaValue().equals(this.target))
        );
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
