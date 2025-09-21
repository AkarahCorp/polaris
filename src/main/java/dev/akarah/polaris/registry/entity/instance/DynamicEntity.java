package dev.akarah.polaris.registry.entity.instance;

import com.google.common.collect.Maps;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.entity.CustomEntity;
import dev.akarah.polaris.registry.item.CustomItem;
import dev.akarah.polaris.script.value.RCell;
import dev.akarah.polaris.script.value.RNullable;
import dev.akarah.polaris.script.value.RNumber;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RIdentifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Projectile;
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

    public EntityEquipment equipment() {
        return this.equipment;
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

        Objects.requireNonNull(this.getAttribute(Attributes.SCALE)).setBaseValue(1);
        this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        this.visual = new VisualEntity(entityType, level, this);

        FAKED_TYPES.put(this.getId(), base.entityType());
        this.setCustomNameVisible(true);
        this.setBoundingBox(this.getDefaultDimensions(Pose.STANDING).makeBoundingBox(0.0, 0.0, 0.0));

        for(var equipment : this.base().equipment().entrySet()) {
            CustomItem.byId(equipment.getValue()).ifPresent(customItem -> {
                this.equipment.set(equipment.getKey(), customItem.toItemStack(RNullable.of(REntity.of(this))));
            });
        }


        this.setComponent(DataComponents.CUSTOM_DATA, base.customData());

        Resources.actionManager().performEvents("entity.spawn", REntity.of(this));
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

        if (this.isInvulnerableTo(serverLevel, damageSource)) {
            return false;
        } else if (this.isDeadOrDying()) {
            return false;
        } else if (damageSource.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        } else if (this.invulnerableTime > 0) {
            return false;
        }

        var cell = RCell.create(RNumber.of(f));
        var proceed = Resources.actionManager().performEvents("entity.take_damage", REntity.of(this), cell);

        if((this.getHealth() - ((Double) RCell.get(cell).javaValue())) <= 0.0) {
            proceed = proceed && Resources.actionManager().performEvents("entity.die", REntity.of(this), cell);
        }

        if(damageSource.getEntity() instanceof ServerPlayer attacker) {
            proceed = proceed && Resources.actionManager().performEvents(
                    "player.attack_entity",
                    REntity.of(attacker),
                    REntity.of(this),
                    cell,
                    RIdentifier.of(damageSource.typeHolder().unwrapKey().orElseThrow().location())
            );

            if((this.getHealth() - ((Double) RCell.get(cell).javaValue())) <= 0.0) {
                proceed = proceed && Resources.actionManager().performEvents("player.kill_entity", REntity.of(attacker), REntity.of(this), cell);
            }
        }

        if(damageSource.getEntity() instanceof Projectile projectile) {
            proceed = proceed && Resources.actionManager().performEvents("projectile.attack_entity", REntity.of(projectile), REntity.of(this), cell);

            if((this.getHealth() - ((Double) RCell.get(cell).javaValue())) <= 0.0) {
                proceed = proceed && Resources.actionManager().performEvents("projectile.kill_entity", REntity.of(projectile), REntity.of(this), cell);
            }
        }

        if(proceed) {
            var result = super.hurtServer(serverLevel, damageSource,  ((Double) (cell.javaValue())).floatValue());

            serverLevel.broadcastDamageEvent(this.visual, damageSource);
            Resources.actionManager().performEvents("entity.take_damage.after", REntity.of(this), cell);
            return result;
        }

        return true;
    }

    @Override
    public void tick() {
        super.tick();

        Resources.actionManager().performEvents("entity.tick", REntity.of(this));
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

    @Override
    public void die(DamageSource damageSource) {


        this.base().lootTable().ifPresent(lootTable -> {
            lootTable.execute((ServerLevel) this.level(), this.position());
        });
        super.die(damageSource);
    }



    @Override
    protected void pushEntities() {

    }
}