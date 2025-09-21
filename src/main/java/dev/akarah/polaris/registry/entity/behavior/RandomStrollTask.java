package dev.akarah.polaris.registry.entity.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.entity.instance.DynamicEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

public record RandomStrollTask(double speedModifier, int interval, boolean checkNoActionTime) implements TaskType {
    public static MapCodec<RandomStrollTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("speed_modifier", 1.0).forGetter(RandomStrollTask::speedModifier),
            Codec.INT.optionalFieldOf("interval", 120).forGetter(RandomStrollTask::interval),
            Codec.BOOL.optionalFieldOf("check_no_action_time", true).forGetter(RandomStrollTask::checkNoActionTime)
    ).apply(instance, RandomStrollTask::new));

    @Override
    public Goal build(DynamicEntity entity) {
        return new RandomStrollGoal(entity, speedModifier, interval, checkNoActionTime);
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
