package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Set;

public interface Behavior {
    BehaviorControl<?> build();
    MapCodec<? extends Behavior> generatorCodec();
    Set<Pair<MemoryModuleType<?>, MemoryStatus>> memoryDependencies();

    Codec<Behavior> CODEC = Codec.lazyInitialized(() -> ExtBuiltInRegistries.BEHAVIOR_TYPE
            .byNameCodec()
            .dispatch(Behavior::generatorCodec, x -> x));

    static MapCodec<? extends Behavior> bootStrap(Registry<MapCodec<? extends Behavior>> registry) {
        Registry.register(registry, ResourceLocation.withDefaultNamespace("random_walk"), RandomWalkBehavior.GENERATOR_CODEC);
        return RandomWalkBehavior.GENERATOR_CODEC;
    }
}
