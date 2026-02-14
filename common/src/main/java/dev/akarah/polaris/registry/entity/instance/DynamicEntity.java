package dev.akarah.polaris.registry.entity.instance;

import com.mojang.math.Transformation;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.entity.CustomEntity;
import dev.akarah.polaris.registry.item.CustomItem;
import dev.akarah.polaris.script.value.RCell;
import dev.akarah.polaris.script.value.RNullable;
import dev.akarah.polaris.script.value.RNumber;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RIdentifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class DynamicEntity extends PathfinderMob implements RangedAttackMob {
    CustomEntity base;
    public LivingEntity wrappedEntity;
    public Display.TextDisplay fakeName;
    static CustomEntity TEMPORARY_BASE;

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


    public static Entity castDynOwner(Entity entity) {
        var cdata = entity.get(DataComponents.CUSTOM_DATA);
        if(cdata == null) {
            return entity;
        }
        var dynOwner = cdata.copyTag().get("polaris:dyn_owner");
        if(dynOwner == null) {
            return entity;
        }
        return entity.level().getEntity(UUID.fromString(dynOwner.asString().orElseThrow()));
    }

    public void applyComponents() {
        this.setComponent(DataComponents.CUSTOM_DATA, this.base().customData());
        for(var slot : this.base().equipment().entrySet()) {
            this.wrappedEntity.setItemSlot(
                    slot.getKey(),
                    CustomItem.byId(slot.getValue()).map(x -> x.toItemStack(RNullable.empty())).orElse(ItemStack.EMPTY)
            );
        }
        this.base().profile().ifPresent(profile -> {
            if(this.wrappedEntity instanceof Mannequin mannequin) {
                mannequin.getEntityData().set(Mannequin.DATA_DESCRIPTION, Optional.empty());
                mannequin.getEntityData().set(Mannequin.DATA_PROFILE, profile);
            }
        });
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

        this.setCustomNameVisible(true);
        this.setBoundingBox(this.getDefaultDimensions(Pose.STANDING).makeBoundingBox(0.0, 0.0, 0.0));

        this.setComponent(DataComponents.CUSTOM_DATA, base.customData());

        var ownerTag = new CompoundTag();
        ownerTag.putString("polaris:dyn_owner", this.getStringUUID());

        // create wrapper entity
        this.wrappedEntity = (LivingEntity) this.base().entityType().create(this.level(), EntitySpawnReason.JOCKEY);
        assert this.wrappedEntity != null;
        this.wrappedEntity.setCustomNameVisible(false);
        this.wrappedEntity.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(ownerTag));
        if(this.wrappedEntity instanceof Mob mob) {
            mob.setNoAi(true);
        }


        // create fake label display
        this.fakeName = new Display.TextDisplay(EntityType.TEXT_DISPLAY, this.level());
        this.fakeName.setText(Component.literal(this.base().name()));
        this.fakeName.startRiding(this.wrappedEntity);
        this.fakeName.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        this.fakeName.setTransformation(new Transformation(
                new Vector3f(0.0f, 0.15f, 0.0f),
                null,
                null,
                null
        ));
        this.fakeName.setBackgroundColor(ARGB.color(50, 0, 0, 0));
        this.fakeName.setTextOpacity((byte) 255);
        this.fakeName.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(ownerTag));

        this.applyComponents();

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
                    RIdentifier.of(damageSource.typeHolder().unwrapKey().orElseThrow().identifier())
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

            serverLevel.broadcastDamageEvent(this.wrappedEntity, damageSource);
            Resources.actionManager().performEvents("entity.take_damage.after", REntity.of(this), cell);
            return result;
        }

        return true;
    }

    @Override
    public void tick() {
        super.tick();

        this.wrappedEntity.setPos(this.getX(), this.getY(), this.getZ());
        this.wrappedEntity.setYRot(this.getYRot());
        this.wrappedEntity.setXRot(this.getXRot());

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
        this.wrappedEntity.remove(removalReason);
        this.fakeName.remove(removalReason);
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        this.setCustomNameVisible(false);
        this.fakeName.setText(component);
    }

    @Override
    public void die(DamageSource damageSource) {
        this.base().lootTable().ifPresent(lootTable -> {
            if(damageSource.getEntity() instanceof ServerPlayer attacker) {
                lootTable.execute((ServerLevel) this.level(), this.position(), attacker);
            } else {

                lootTable.execute((ServerLevel) this.level(), this.position());
            }
        });
        super.die(damageSource);

        this.wrappedEntity.remove(RemovalReason.DISCARDED);
        this.fakeName.remove(RemovalReason.DISCARDED);
    }

    @Override
    protected void pushEntities() {

    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return true;
    }

    @Override
    public void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }


}