package dev.akarah.cdata.script.value.mc;

import dev.akarah.cdata.db.Database;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import dev.akarah.cdata.registry.entity.VisualEntity;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.*;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainer;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.MenuType;

public class REntity extends RuntimeValue {
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

    @MethodTypeHint(signature = "(this: entity) -> store", documentation = "Gets the temporary data store associated with this entity.")
    public static RStore temp_data(REntity $this) {
        return RStore.of(Database.temp().get("entity/" + $this.javaValue().getStringUUID()));
    }

    @MethodTypeHint(signature = "(this: entity) -> store", documentation = "Gets the persistent data store associated with this entity. If not used on a player, this will return the temporary data store instead.")
    public static RStore save_data(REntity $this) {
        if($this.javaValue() instanceof ServerPlayer) {
            return RStore.of(Database.save().get("entity/" + $this.javaValue().getStringUUID()));
        }
        return REntity.temp_data($this);
    }

    @MethodTypeHint(signature = "(this: entity) -> vector", documentation = "Returns the position of this entity.")
    public static RVector position(REntity $this) {
        return RVector.of($this.inner.position());
    }

    @MethodTypeHint(signature = "(this: entity) -> vector", documentation = "Returns the direction of this entity, in the form of an X/Y/Z vector.")
    public static RVector direction(REntity $this) {
        return RVector.of($this.inner.getLookAngle());
    }

    @MethodTypeHint(signature = "(this: entity, position: vector) -> void", documentation = "Teleports the entity to the given position.")
    public static void teleport(REntity $this, RVector vector) {
        $this.inner.teleportTo(vector.javaValue().x, vector.javaValue().y, vector.javaValue().z);
    }

    @MethodTypeHint(signature = "(this: entity, message: text) -> void", documentation = "Sends a message in the chat of the player provided.")
    public static void send_message(REntity $this, RText message) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message.javaValue());
        }
    }

    @MethodTypeHint(signature = "(this: entity, message: text) -> void", documentation = "Sends a message through the actionbar to the player provided.")
    public static void send_actionbar(REntity $this, RText message) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message.javaValue(), true);
        }
    }

    @MethodTypeHint(signature = "(this: entity) -> string", documentation = "Gets the name of the provided entity.")
    public static RString name(REntity $this) {
        if($this.inner instanceof DynamicEntity dynamicEntity) {
            return RString.of(dynamicEntity.base().name());
        }
        if($this.inner instanceof VisualEntity visual) {
            return RString.of(visual.dynamic().base().name());
        }
        if($this.inner instanceof ServerPlayer serverPlayer) {
            return RString.of(serverPlayer.getName().getString());
        }
        return RString.of("Unnamed");
    }

    @MethodTypeHint(signature = "(this: entity, name: text) -> void", documentation = "Sets the name of the given entity.")
    public static void set_name(REntity $this, RText message) {
        $this.inner.setCustomName(message.javaValue());
    }

    @MethodTypeHint(signature = "(this: entity) -> world", documentation = "Gets the world the entity is located in.")
    public static RWorld world(REntity $this) {
        return RWorld.of((ServerLevel) $this.inner.level());
    }

    @MethodTypeHint(signature = "(this: entity) -> number", documentation = "Gets the current health of the entity.")
    public static RNumber health(REntity $this) {
        if($this.inner instanceof LivingEntity le) {
            return RNumber.of(le.getHealth());
        }
        return RNumber.of(0.0);
    }

    @MethodTypeHint(signature = "(this: entity, health: number) -> void", documentation = "Sets the health of the entity.")
    public static void set_health(REntity $this, RNumber number) {
        if($this.inner instanceof LivingEntity le) {
            le.setHealth((float) number.doubleValue());
        }
    }

    @MethodTypeHint(signature = "(this: entity) -> identifier", documentation = "Returns the entity type of this entity.")
    public static RIdentifier type(REntity $this) {
        if($this.inner instanceof DynamicEntity le) {
            return RIdentifier.of(le.base().id());
        }
        return RIdentifier.of($this.inner.getType().builtInRegistryHolder().key().location());
    }

    @MethodTypeHint(signature = "(this: entity) -> inventory", documentation = "Gets the inventory of the entity, returning a 0-slot inventory if it is not a player.")
    public static RInventory inventory(REntity $this) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            return RInventory.of(serverPlayer.getInventory());
        }
        return RInventory.of(new DynamicContainer(0));
    }

    @MethodTypeHint(signature = "(this: entity, inv: inventory) -> void", documentation = "Opens an inventory for the player.")
    public static void open_inventory(REntity $this, RInventory inventory) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            MenuProvider mp = null;
            switch (inventory.javaValue().getContainerSize()) {
                case 27 -> mp = new SimpleMenuProvider((id, playerInventory, _) -> new DynamicContainerMenu(
                        MenuType.GENERIC_9x3, id,
                        playerInventory, inventory.javaValue(),
                        3
                ), inventory.name.javaValue());
            }
            if(mp != null) {
                serverPlayer.openMenu(mp);
            }
        }
    }

    @MethodTypeHint(signature = "(this: entity, stat: string) -> number", documentation = "Gets the stat key for the given player, returning 0.0 as a default.")
    public static RNumber stat(REntity $this, RString key) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            return RNumber.of(Resources.statManager().lookup(serverPlayer).get(key.javaValue()));
        }
        return RNumber.of(0.0);
    }

    @MethodTypeHint(signature = "(this: entity, attribute: identifier, value: number) -> void", documentation = "Sets the base value of the given attribute on the entity.")
    public static void set_attribute(REntity $this, RIdentifier key, RNumber value) {
        if($this.inner instanceof LivingEntity le) {
            BuiltInRegistries.ATTRIBUTE.get(key.javaValue()).ifPresent(attributeReference -> {
                var attr = le.getAttribute(attributeReference);
                if(attr != null) {
                    attr.setBaseValue(value.doubleValue());
                }
            });
        }
    }

    @MethodTypeHint(signature = "(this: entity) -> uuid", documentation = "Returns the UUID of the entity.")
    public static RUuid uuid(REntity $this) {
        return RUuid.of($this.inner.getUUID());
    }
}
