package dev.akarah.cdata.registry.entity.behavior;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;

import java.util.Set;

public record RandomWalkBehavior(
        float speed,
        boolean canSwimInWater,
        int distanceX,
        int distanceZ
) implements Behavior {
    public static MapCodec<RandomWalkBehavior> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("speed").forGetter(RandomWalkBehavior::speed),
            Codec.BOOL.optionalFieldOf("can_swim_in_water", true).forGetter(RandomWalkBehavior::canSwimInWater),
            Codec.INT.optionalFieldOf("dx", 7).forGetter(RandomWalkBehavior::distanceX),
            Codec.INT.optionalFieldOf("dz", 7).forGetter(RandomWalkBehavior::distanceZ)
    ).apply(instance, RandomWalkBehavior::new));

    @Override
    public BehaviorControl<?> build() {
        return RandomStroll.strollFlyOrSwim(
                this.speed,
                pathfinderMob -> LandRandomPos.getPos(
                        pathfinderMob,
                        this.distanceX,
                        this.distanceZ
                ),
                this.canSwimInWater ? _ -> true : pathfinderMob -> !pathfinderMob.isInWater()
        );
    }

    @Override
    public MapCodec<? extends Behavior> generatorCodec() {
        return GENERATOR_CODEC;
    }

    @Override
    public Set<Pair<MemoryModuleType<?>, MemoryStatus>> memoryDependencies() {
        return Set.of(
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED)
        );
    }
}
