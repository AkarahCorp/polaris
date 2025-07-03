package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;

import java.util.Set;

public record RandomWalkTask(
        float speed,
        boolean canSwimInWater,
        double radiusXZ,
        double radiusY
) implements TaskType {
    public static MapCodec<RandomWalkTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("speed").forGetter(RandomWalkTask::speed),
            Codec.BOOL.optionalFieldOf("can_swim_in_water", true).forGetter(RandomWalkTask::canSwimInWater),
            Codec.DOUBLE.optionalFieldOf("dxz", 7.0).forGetter(RandomWalkTask::radiusXZ),
            Codec.DOUBLE.optionalFieldOf("dy", 7.0).forGetter(RandomWalkTask::radiusY)
    ).apply(instance, RandomWalkTask::new));

    @Override
    public ExtendedBehaviour<? super DynamicEntity> build() {
        return new SetRandomWalkTarget<>()
                .speedModifier(this.speed)
                .setRadius(this.radiusXZ, this.radiusY);
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
