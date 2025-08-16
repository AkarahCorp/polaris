package dev.akarah.polaris.registry.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.math.Transformation;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.registry.Resources;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public class VisualEntity extends LivingEntity {
    DynamicEntity dynamic;
    FakePlayer fakePlayer;
    Display.TextDisplay fakeName;

    public DynamicEntity dynamic() {
        return this.dynamic;
    }

    protected VisualEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level,
            DynamicEntity dynamicEntity
    ) {
        super(entityType, level);
        this.dynamic = dynamicEntity;

        DynamicEntity.FAKED_TYPES.put(this.getId(), this.dynamic.base().entityType());

        if(this.dynamic.base().entityType().equals(EntityType.PLAYER)) {
            var gameProfile = Resources.GAME_PROFILES.get(this.dynamic().base().playerSkinName().orElseThrow());

            var newProfile = new GameProfile(this.uuid, "");
            gameProfile.getProperties().forEach((k, p) -> newProfile.getProperties().put(k, p));

            var fp = FakePlayer.get((ServerLevel) level, newProfile);
            this.fakePlayer = fp;
            CustomEntity.FAKE_PLAYERS.add(this.fakePlayer);

            fp.setUUID(this.getUUID());
            fp.setId(this.getId());
            fp.setCustomNameVisible(true);
            fp.setCustomName(Component.literal("Placeholder"));

            // todo: add skin layers

            var packet = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(
                            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
                    ),
                    Set.of(fp)
            );

            for(var p : Main.server().getPlayerList().getPlayers()) {
                p.connection.send(packet);
            }
        }

        this.fakeName = new Display.TextDisplay(EntityType.TEXT_DISPLAY, this.level());
        this.fakeName.setText(Component.literal(this.dynamic().base().name()));
        this.fakeName.startRiding(this);
        this.fakeName.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        this.fakeName.setTransformation(new Transformation(
                new Vector3f(0.0f, 0.15f, 0.0f),
                null,
                null,
                null
        ));
        this.fakeName.setBackgroundColor(ARGB.color(50, 0, 0, 0));
        this.fakeName.setTextOpacity((byte) 255);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        var packet = super.getAddEntityPacket(serverEntity);
        if(packet instanceof ClientboundAddEntityPacket addEntityPacket) {
            addEntityPacket.type = DynamicEntity.FAKED_TYPES.get(this.getId());
        }
        return packet;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return dynamic().hurtServer(serverLevel, damageSource, f);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {

    }

    @Override
    public void tick() {
        super.tick();
        for(var slot : EquipmentSlot.values()) {
            this.equipment.set(slot, this.dynamic().equipment().get(slot));
        }
        this.teleportTo(this.dynamic().getX(), this.dynamic.getY(), this.dynamic.getZ());
        this.setRot(this.dynamic.getYRot(), this.dynamic.getXRot());
        this.setYHeadRot(this.dynamic.getYHeadRot());
        this.setXRot(this.dynamic.getXRot());
        this.setYRot(this.dynamic.getYRot());
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void onRemoval(RemovalReason removalReason) {
        super.onRemoval(removalReason);
        if(this.fakePlayer != null) {
            this.fakePlayer.remove(removalReason);
        }
        CustomEntity.FAKE_PLAYERS.remove(this.fakePlayer);
        DynamicEntity.FAKED_TYPES.remove(this.getId());
        this.fakeName.remove(removalReason);
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        super.setCustomName(component);
        this.fakeName.setText(component);
    }
}