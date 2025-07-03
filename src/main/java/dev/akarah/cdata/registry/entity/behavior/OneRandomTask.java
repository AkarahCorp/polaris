package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;

import java.util.List;

public record OneRandomTask(List<TaskType> tasks) implements TaskType {
    public static MapCodec<OneRandomTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TaskType.CODEC.listOf().fieldOf("tasks").forGetter(OneRandomTask::tasks)
    ).apply(instance, OneRandomTask::new));

    @Override
    @SuppressWarnings("unchecked")
    public ExtendedBehaviour<? super DynamicEntity> build() {
        return new OneRandomBehaviour<>(
                this.tasks.stream().map(TaskType::build).map(x -> Pair.of(x, 1)).toList()
                        .toArray(Pair[]::new)
        );
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
