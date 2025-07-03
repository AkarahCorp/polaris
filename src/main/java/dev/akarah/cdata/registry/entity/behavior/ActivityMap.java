package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Map;

public record ActivityMap(
        Map<Activity, List<TaskType>> activities
) {
    public static Codec<ActivityMap> CODEC = Codec.unboundedMap(
            BuiltInRegistries.ACTIVITY.byNameCodec(),
            TaskType.CODEC.listOf()
    ).xmap(ActivityMap::new, ActivityMap::activities);

}
