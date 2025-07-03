package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;

public record MoveToTargetTask() implements TaskType {
    public static MapCodec<MoveToTargetTask> GENERATOR_CODEC = MapCodec.unit(MoveToTargetTask::new);

    @Override
    public ExtendedBehaviour<? super DynamicEntity> build() {
        return new MoveToWalkTarget<>();
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
