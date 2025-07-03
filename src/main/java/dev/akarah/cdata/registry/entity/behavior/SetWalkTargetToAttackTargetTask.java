package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;

public record SetWalkTargetToAttackTargetTask(float speed) implements TaskType {
    public static MapCodec<SetWalkTargetToAttackTargetTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("speed").forGetter(SetWalkTargetToAttackTargetTask::speed)
    ).apply(instance, SetWalkTargetToAttackTargetTask::new));

    @Override
    public ExtendedBehaviour<? super DynamicEntity> build() {
        return new SetWalkTargetToAttackTarget<>()
                .speedMod(this.speed);
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
