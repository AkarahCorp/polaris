package dev.akarah.polaris.registry.entity.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.entity.DynamicEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public record MeleeAttackTask(double speedModifier, boolean followTarget) implements TaskType {
    public static MapCodec<MeleeAttackTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("speed_modifier", 1.0).forGetter(MeleeAttackTask::speedModifier),
            Codec.BOOL.optionalFieldOf("follow_target", true).forGetter(MeleeAttackTask::followTarget)
    ).apply(instance, MeleeAttackTask::new));

    @Override
    public Goal build(DynamicEntity entity) {
        return new MeleeAttackGoal(entity, speedModifier, followTarget);
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
