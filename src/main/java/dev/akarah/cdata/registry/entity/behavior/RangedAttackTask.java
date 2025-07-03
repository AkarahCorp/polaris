package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableRangedAttack;

public record RangedAttackTask(int delayTicks) implements TaskType {
    public static MapCodec<RangedAttackTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("delay").forGetter(RangedAttackTask::delayTicks)
    ).apply(instance, RangedAttackTask::new));

    @Override
    public ExtendedBehaviour<? super DynamicEntity> build() {
        return new AnimatableRangedAttack<>(this.delayTicks);
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
