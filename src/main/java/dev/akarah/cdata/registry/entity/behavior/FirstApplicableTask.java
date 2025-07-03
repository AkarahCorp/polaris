package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;

import java.util.ArrayList;
import java.util.List;

public record FirstApplicableTask(List<TaskType> tasks) implements TaskType {
    public static MapCodec<FirstApplicableTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TaskType.CODEC.listOf().fieldOf("tasks").forGetter(FirstApplicableTask::tasks)
    ).apply(instance, FirstApplicableTask::new));

    @Override
    @SuppressWarnings("unchecked")
    public ExtendedBehaviour<? super DynamicEntity> build() {
        return new FirstApplicableBehaviour<>(
                this.tasks.stream().map(TaskType::build).map(x -> Pair.of(x, 1)).toList()
                        .toArray(Pair[]::new)
        );
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
