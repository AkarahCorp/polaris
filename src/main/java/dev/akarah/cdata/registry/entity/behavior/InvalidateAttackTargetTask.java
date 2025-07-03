package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.minecraft.world.entity.EntityType;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;

import java.util.List;
import java.util.Optional;

public record InvalidateAttackTargetTask() implements TaskType {
    public static MapCodec<InvalidateAttackTargetTask> GENERATOR_CODEC = MapCodec.unit(InvalidateAttackTargetTask::new);

    @Override
    public ExtendedBehaviour<? super DynamicEntity> build() {
        return new InvalidateAttackTarget<>();
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
