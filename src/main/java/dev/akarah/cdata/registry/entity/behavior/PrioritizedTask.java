package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PrioritizedTask(
        int priority,
        TaskType task
) {
    public static Codec<PrioritizedTask> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("priority").forGetter(PrioritizedTask::priority),
            TaskType.CODEC.fieldOf("task").forGetter(PrioritizedTask::task)
    ).apply(instance, PrioritizedTask::new));
}
