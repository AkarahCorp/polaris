package dev.akarah.cdata.registry.entity;

import com.google.common.collect.Maps;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.registry.entity.behavior.TaskType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DynamicEntity extends PathfinderMob implements RangedAttackMob {
    CustomEntity base;
    static Lock lock = new ReentrantLock();
    static CustomEntity TEMPORARY_BASE;
    public static Map<Integer, EntityType<?>> FAKED_TYPES = Maps.newHashMap();

    public CustomEntity base() {
        return this.base;
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

    @Override
    protected @NotNull EntityDimensions getDefaultDimensions(Pose pose) {
        return this.base().entityType().getDimensions();
    }

    @Override
    protected AABB getHitbox() {
        return this.getBoundingBox();
    }

    private DynamicEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level,
            CustomEntity base
    ) {
        super(entityType, level);
        this.base = base;
        FAKED_TYPES.put(this.getId(), base.entityType());
        this.setCustomNameVisible(true);
        this.setBoundingBox(this.getDefaultDimensions(Pose.STANDING).makeBoundingBox(0.0, 0.0, 0.0));

        this.base().events()
                .flatMap(EntityEvents::onSpawn)
                .ifPresent(x -> ExtReloadableResources.actionManager().callFunctions(x, List.of(this)));
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
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
        if(packet instanceof ClientboundAddEntityPacket addEntityPacket) {
            addEntityPacket.type = FAKED_TYPES.get(this.getId());
        }
        return packet;
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {

    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        var result = super.hurtServer(serverLevel, damageSource, f);
        this.base().events()
                .flatMap(EntityEvents::onTakeDamage)
                .ifPresent(x -> ExtReloadableResources.actionManager().callFunctions(x, List.of(this)));

        if(this.getHealth() <= 0.0) {
            this.base().events()
                    .flatMap(EntityEvents::onDeath)
                    .ifPresent(x -> ExtReloadableResources.actionManager().callFunctions(x, List.of(this)));
        }

        if(damageSource.getEntity() instanceof ServerPlayer attacker) {
            this.base().events()
                    .flatMap(EntityEvents::onPlayerAttack)
                    .ifPresent(x -> ExtReloadableResources.actionManager().callFunctions(x, List.of(attacker, this)));

            if(this.getHealth() <= 0.0) {
                this.base().events()
                        .flatMap(EntityEvents::onPlayerKill)
                        .ifPresent(x -> ExtReloadableResources.actionManager().callFunctions(x, List.of(attacker, this)));
            }
        }
        return result;

    }

    @Override
    public void tick() {
        super.tick();

        this.base().events()
                .flatMap(EntityEvents::onTick)
                .ifPresent(x -> ExtReloadableResources.actionManager().callFunctions(x, List.of(this)));
    }

    @Override
    protected void registerGoals() {
        TEMPORARY_BASE.behaviorGoals().stream().flatMap(Collection::stream).forEach(goal -> {
            this.goalSelector.addGoal(goal.priority(), goal.task().build(this));
        });
        TEMPORARY_BASE.targetGoals().stream().flatMap(Collection::stream).forEach(goal -> {
            this.targetSelector.addGoal(goal.priority(), goal.task().build(this));
        });
    }
}