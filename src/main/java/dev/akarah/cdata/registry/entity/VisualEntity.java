package dev.akarah.cdata.registry.entity;

import com.mojang.authlib.GameProfile;
import dev.akarah.cdata.Main;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VisualEntity extends Entity {
    DynamicEntity dynamic;
    FakePlayer fakePlayer;
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


        var gameProfile = CustomEntity.GAME_PROFILES.get(this.dynamic().base().playerSkinName());

        var newProfile = new GameProfile(this.uuid, gameProfile.getName());
        gameProfile.getProperties().forEach((k, p) -> {
            newProfile.getProperties().put(k, p);
        });

        var fp = FakePlayer.get((ServerLevel) level, newProfile);
        this.fakePlayer = fp;
        CustomEntity.FAKE_PLAYERS.add(this.fakePlayer);

        fp.setUUID(this.getUUID());
        fp.setId(this.getId());
        fp.setCustomNameVisible(true);
        fp.setCustomName(Component.literal("Placeholder"));

        // todo: add skin layers
        // todo: make the name render correctly via a passenger text display

        var packet = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME), Set.of(fp));

        for(var p : Main.server().getPlayerList().getPlayers()) {
            p.connection.send(packet);
        }
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
        this.teleportTo(this.dynamic().getX(), this.dynamic.getY(), this.dynamic.getZ());
        this.setRot(this.dynamic.getYRot(), this.dynamic.getXRot());
        this.setYHeadRot(this.dynamic.getYHeadRot());

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public void onRemoval(RemovalReason removalReason) {
        super.onRemoval(removalReason);
        if(this.fakePlayer != null) {
            this.fakePlayer.remove(removalReason);
        }
        CustomEntity.FAKE_PLAYERS.remove(this.fakePlayer);
        DynamicEntity.FAKED_TYPES.remove(this.getId());
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        super.setCustomName(component);
        if(this.fakePlayer != null) {
            this.fakePlayer.setCustomName(component);
        }
    }
}