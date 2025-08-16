package dev.akarah.polaris.registry.entity.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.polaris.registry.ExtBuiltInRegistries;
import dev.akarah.polaris.registry.entity.DynamicEntity;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;

public interface TaskType {
    Goal build(DynamicEntity entity);
    MapCodec<? extends TaskType> generatorCodec();

    Codec<TaskType> CODEC = Codec.lazyInitialized(() -> ExtBuiltInRegistries.BEHAVIOR_TYPE
            .byNameCodec()
            .dispatch(TaskType::generatorCodec, x -> x));

    static MapCodec<? extends TaskType> bootStrap(Registry<MapCodec<? extends TaskType>> registry) {
        Registry.register(registry, ResourceLocation.withDefaultNamespace("random_stroll"), RandomStrollTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("melee_attack"), MeleeAttackTask.GENERATOR_CODEC);
        Registry.register(registry, ResourceLocation.withDefaultNamespace("nearest_attackable_target"), NearestAttackableTargetTask.GENERATOR_CODEC);
        return RandomStrollTask.GENERATOR_CODEC;
    }
}
