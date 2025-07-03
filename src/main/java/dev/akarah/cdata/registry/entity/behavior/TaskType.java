package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

public interface TaskType {
    ExtendedBehaviour<? super DynamicEntity> build();
    MapCodec<? extends TaskType> generatorCodec();

    Codec<TaskType> CODEC = Codec.lazyInitialized(() -> ExtBuiltInRegistries.BEHAVIOR_TYPE
            .byNameCodec()
            .dispatch(TaskType::generatorCodec, x -> x));

    static MapCodec<? extends TaskType> bootStrap(Registry<MapCodec<? extends TaskType>> registry) {
        Registry.register(registry, ResourceLocation.withDefaultNamespace("random_walk"), RandomWalkTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("look_at_target"), LookAtTargetTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("move_to_target"), MoveToTargetTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("random"), OneRandomTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("first_applicable"), FirstApplicableTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("target_or_retaliate"), TargetOrRetaliateTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("invalidate_attack_target"), InvalidateAttackTargetTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("move_to_attack_target"), SetWalkTargetToAttackTargetTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("melee_attack"), MeleeAttackTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("ranged_attack"), RangedAttackTask.GENERATOR_CODEC);
        return RandomWalkTask.GENERATOR_CODEC;
    }
}
