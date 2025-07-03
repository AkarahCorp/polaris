package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.minecraft.world.entity.EntityType;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;

import java.util.List;
import java.util.Optional;

public record TargetOrRetaliateTask(Optional<List<EntityType<?>>> targets) implements TaskType {
    public static MapCodec<TargetOrRetaliateTask> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityType.CODEC.listOf().optionalFieldOf("targets").forGetter(TargetOrRetaliateTask::targets)
    ).apply(instance, TargetOrRetaliateTask::new));

    @Override
    public ExtendedBehaviour<? super DynamicEntity> build() {
        return new TargetOrRetaliate<>()
                .attackablePredicate(le -> targets.map(x -> x.contains(le.getType())).orElse(true));
    }

    @Override
    public MapCodec<? extends TaskType> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
