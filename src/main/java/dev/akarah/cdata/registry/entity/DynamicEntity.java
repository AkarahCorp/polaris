package dev.akarah.cdata.registry.entity;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.JsonOps;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.registry.entity.behavior.TaskType;
import dev.akarah.cdata.registry.item.CustomItem;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DynamicEntity extends PathfinderMob implements RangedAttackMob {
    CustomEntity base;
    public VisualEntity visual;
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
        TEMPORARY_BASE = base;
        return new DynamicEntity(entityType, level, base);
    }

    @Override
    protected @NotNull EntityDimensions getDefaultDimensions(Pose pose) {
        return this.base().entityType().getDimensions();
    }

    @Override
    protected @NotNull AABB getHitbox() {
        return this.getBoundingBox();
    }

    private DynamicEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level,
            CustomEntity base
    ) {
        super(entityType, level);
        this.base = base;
        Objects.requireNonNull(this.getAttribute(Attributes.SCALE)).setBaseValue(0.01);
        this.visual = new VisualEntity(entityType, level, this);

        FAKED_TYPES.put(this.getId(), base.entityType());
        this.setCustomNameVisible(true);
        this.setBoundingBox(this.getDefaultDimensions(Pose.STANDING).makeBoundingBox(0.0, 0.0, 0.0));

        for(var equipment : this.base().equipment().entrySet()) {
            CustomItem.byId(equipment.getValue()).ifPresent(customItem -> {
                this.equipment.set(equipment.getKey(), customItem.toItemStack());
            });
        }

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
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        var packet = super.getAddEntityPacket(serverEntity);
        if(packet instanceof ClientboundAddEntityPacket addEntityPacket) {
            addEntityPacket.type = EntityType.ZOMBIE;
        }
        return packet;
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {

    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if(this.base().invulnerable()) {
            return false;
        }
        var result = super.hurtServer(serverLevel, damageSource, f);
        if(result) {
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

    @Override
    public void onRemoval(RemovalReason removalReason) {
        super.onRemoval(removalReason);
        this.visual.remove(removalReason);
        FAKED_TYPES.remove(this.getId());
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        this.setCustomNameVisible(false);
        this.visual.setCustomName(component);
        this.visual.setCustomNameVisible(true);
    }
}