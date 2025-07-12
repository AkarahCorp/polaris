package dev.akarah.cdata.script.value;

import dev.akarah.cdata.registry.entity.DynamicEntity;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class REntity extends RuntimeValue<Entity> {
    private final Entity inner;

    private REntity(Entity inner) {
        this.inner = inner;
    }

    public static REntity of(Entity value) {
        return new REntity(value);
    }

    @Override
    public Entity javaValue() {
        return this.inner;
    }

    @MethodTypeHint("(this: entity) -> vector")
    public static RVector position(REntity $this) {
        return RVector.of($this.inner.position());
    }

    @MethodTypeHint("(this: entity) -> vector")
    public static RVector direction(REntity $this) {
        return RVector.of($this.inner.getLookAngle());
    }

    @MethodTypeHint("(this: entity, position: vector) -> void")
    public static void teleport(REntity $this, RVector vector) {
        $this.inner.teleportTo(vector.javaValue().x, vector.javaValue().y, vector.javaValue().z);
    }

    @MethodTypeHint("(this: entity, message: text) -> void")
    public static void send_message(REntity $this, RText message) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message.javaValue());
        }
    }

    @MethodTypeHint("(this: entity, message: text) -> void")
    public static void send_actionbar(REntity $this, RText message) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message.javaValue(), true);
        }
    }

    @MethodTypeHint("(this: entity, name: text) -> void")
    public static void set_name(REntity $this, RText message) {
        $this.inner.setCustomName(message.javaValue());
    }

    @MethodTypeHint("(this: entity) -> world")
    public static RWorld world(REntity $this) {
        return RWorld.of((ServerLevel) $this.inner.level());
    }

    @MethodTypeHint("(this: entity) -> number")
    public static RNumber health(REntity $this) {
        if($this.inner instanceof LivingEntity le) {
            return RNumber.of(le.getHealth());
        }
        return RNumber.of(0.0);
    }

    @MethodTypeHint("(this: entity, health: number) -> void")
    public static void set_health(REntity $this, RNumber number) {
        if($this.inner instanceof LivingEntity le) {
            le.setHealth((float) number.doubleValue());
        }
    }

    @MethodTypeHint("(this: entity) -> identifier")
    public static RIdentifier type(REntity $this) {
        if($this.inner instanceof DynamicEntity le) {
            return RIdentifier.of(le.base().id());
        }
        return RIdentifier.of($this.inner.getType().builtInRegistryHolder().key().location());
    }
}
