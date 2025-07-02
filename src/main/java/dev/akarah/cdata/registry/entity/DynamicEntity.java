package dev.akarah.cdata.registry.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DynamicEntity extends PathfinderMob {
    CustomEntity base;
    static Lock lock = new ReentrantLock();
    static CustomEntity TEMPORARY_BASE;
    public static Map<Integer, EntityType<?>> FAKED_TYPES = Maps.newHashMap();

    private static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.copyOf(
            BuiltInRegistries.MEMORY_MODULE_TYPE.listElements().map(Holder.Reference::value).toList()
    );


    private static final ImmutableList<? extends SensorType<? extends Sensor<? super DynamicEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_ADULT_ANY_TYPE,
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.DUMMY,
            SensorType.NEAREST_PLAYERS,
            SensorType.NEAREST_BED,
            SensorType.NEAREST_ITEMS,
            SensorType.HURT_BY,
            SensorType.IS_IN_WATER
    );

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Brain<DynamicEntity> getBrain() {
        return (Brain<DynamicEntity>) super.getBrain();
    }

    @Override
    protected Brain.@NotNull Provider<DynamicEntity> brainProvider() {
        return Brain.provider(DynamicEntity.MEMORY_TYPES, DynamicEntity.SENSOR_TYPES);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected @NotNull Brain<DynamicEntity> makeBrain(Dynamic<?> dynamic) {
        var entries = new ArrayList<Pair<Integer, ? extends BehaviorControl<? super DynamicEntity>>>();
        System.out.println(TEMPORARY_BASE.behaviors());
        for(var entry : TEMPORARY_BASE.behaviors()) {
            entries.add(
                    Pair.of(
                            entry.priority(),
                            (BehaviorControl<? super DynamicEntity>) entry.behavior().build()
                    )
            );
        }

        var brain = this.brainProvider().makeBrain(dynamic);
        brain.addActivity(
                Activity.CORE,
                ImmutableList.of(
                        Pair.of(
                                1,
                                new MoveToTargetSink()
                        )
                )
        );
        brain.addActivity(
                Activity.IDLE,
                ImmutableList.copyOf(entries)
        );

        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        brain.setActiveActivityIfPossible(Activity.IDLE);
        brain.setSchedule(Schedule.EMPTY);

        return brain;
    }

    public static DynamicEntity create(
            EntityType<? extends PathfinderMob> entityType,
            Level level,
            CustomEntity base
    ) {
        lock.lock();
        TEMPORARY_BASE = base;
        var e = new DynamicEntity(entityType, level, base);
        lock.unlock();
        return e;
    }

    private DynamicEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level,
            CustomEntity base
    ) {
        super(entityType, level);
        this.base = base;
        FAKED_TYPES.put(this.getId(), base.entityType());
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("dynamicEntityBrain");
        this.getBrain().tick(serverLevel, this);
        profilerFiller.pop();
        this.getBrain().setActiveActivityIfPossible(Activity.CORE);
        super.customServerAiStep(serverLevel);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void remove(RemovalReason removalReason) {
        super.remove(removalReason);
        FAKED_TYPES.remove(this.getId());
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        var packet = super.getAddEntityPacket(serverEntity);
        System.out.println(packet);
        if(packet instanceof ClientboundAddEntityPacket addEntityPacket) {
            addEntityPacket.type = FAKED_TYPES.get(this.getId());
        }
        return packet;
    }
}