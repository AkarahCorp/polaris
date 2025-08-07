package dev.akarah.cdata.script.value.mc;

import com.google.common.collect.Maps;
import dev.akarah.cdata.db.Database;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import dev.akarah.cdata.registry.entity.VisualEntity;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.*;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainer;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
            return RInventory.of(serverPlayer.getInventory(), RText.of(Component.literal("Player Inventory")));
        }
        return RInventory.of(new DynamicContainer(0), RText.of(Component.literal("Default Inventory")));
    }

    @MethodTypeHint(signature = "(this: entity) -> nullable[inventory]", documentation = "Gets the open inventory of the entity.")
    public static RNullable current_open_inventory(REntity $this) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer && serverPlayer.hasContainerOpen()) {
            var inv = RInventory.of(serverPlayer.containerMenu.getSlot(0).container, RText.of(Component.literal("Untitled")));
            if(serverPlayer.containerMenu instanceof DynamicContainerMenu dynamicContainerMenu) {
                inv.name = dynamicContainerMenu.name;
            }
            return RNullable.of(inv);
        }
        return RNullable.empty();
    }

    @MethodTypeHint(signature = "(this: entity, inv: inventory) -> void", documentation = "Opens an inventory for the player.")
    public static void open_inventory(REntity $this, RInventory inventory) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            var mt = switch (inventory.javaValue().getContainerSize()) {
                case 9 -> MenuType.GENERIC_9x1;
                case 18 -> MenuType.GENERIC_9x2;
                case 27 -> MenuType.GENERIC_9x3;
                case 36 -> MenuType.GENERIC_9x4;
                case 45 -> MenuType.GENERIC_9x5;
                case 54 -> MenuType.GENERIC_9x6;
                case 5 -> MenuType.HOPPER;
                default -> throw new RuntimeException();
            };

            serverPlayer.openMenu(
                    new SimpleMenuProvider((id, playerInventory, _) -> new DynamicContainerMenu(
                            mt, id,
                            playerInventory, inventory.javaValue(),
                            inventory.javaValue().getContainerSize() / 9,
                            inventory.name
                    ), inventory.name.javaValue())
            );
        }
    }

    @MethodTypeHint(signature = "(this: entity, stat: string) -> number", documentation = "Gets the stat key for the given player, returning 0.0 as a default.")
    public static RNumber stat(REntity $this, RString key) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            return RNumber.of(Resources.statManager().lookup(serverPlayer).get(key.javaValue()));
        }
        if($this.inner instanceof DynamicEntity dynamicEntity) {
            return RNumber.of(dynamicEntity.base().stats().map(x -> x.get(key.javaValue())).orElse(0.0));
        }
        return RNumber.of(0.0);
    }

    @MethodTypeHint(signature = "(this: entity) -> stat_obj", documentation = "Gets the stat key for the given player, returning 0.0 as a default.")
    public static RStatsObject stats(REntity $this) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            return RStatsObject.of(Resources.statManager().lookup(serverPlayer));
        }
        if($this.inner instanceof DynamicEntity dynamicEntity) {
            return RStatsObject.of(dynamicEntity.base().stats().orElse(StatsObject.of()));
        }
        return RStatsObject.of(StatsObject.of());
    }

    @MethodTypeHint(signature = "(this: entity, attribute: identifier, value: number) -> void", documentation = "Sets the base value of the given attribute on the entity.")
    public static void set_attribute(REntity $this, RIdentifier key, RNumber value) {
        if($this.inner instanceof LivingEntity le) {
            BuiltInRegistries.ATTRIBUTE.get(key.javaValue()).ifPresent(attributeReference -> {
                var attr = le.getAttribute(attributeReference);
                if(le instanceof DynamicEntity dynamicEntity && key.toString().equals("minecraft:scale")) {
                    attr = dynamicEntity.visual.getAttribute(attributeReference);
                }
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

    @MethodTypeHint(signature = "(this: entity, stats: stat_obj) -> void", documentation = "Returns the UUID of the entity.")
    public static void add_stats(REntity $this, RStatsObject statsObject) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            var so = Resources.statManager().lookup(serverPlayer);
            so.add(statsObject.javaValue());
            Resources.statManager().set(serverPlayer, so);
        }
    }

    @MethodTypeHint(signature = "(this: entity) -> nullable[uuid]", documentation = "Returns the UUID of the entity.")
    public static RNullable owner(REntity $this) {
        if($this.javaValue() instanceof ItemEntity item) {
            if(item.target == null) {
                return RNullable.empty();
            }
            return RNullable.of(RUuid.of(item.target));
        }
        return RNullable.empty();
    }

    @MethodTypeHint(signature = "(this: entity) -> nullable[item]", documentation = "Returns the UUID of the entity.")
    public static RNullable item(REntity $this) {
        if($this.javaValue() instanceof ItemEntity item) {
            return RNullable.of(RItem.of(item.getItem()));
        }
        return RNullable.empty();
    }

    @MethodTypeHint(signature = "(this: entity) -> void", documentation = "Returns the UUID of the entity.")
    public static void remove(REntity $this) {
        $this.javaValue().remove(Entity.RemovalReason.KILLED);
    }

    public static Map<UUID, Scoreboard> scoreboards = Maps.newHashMap();
    public static Map<UUID, Objective> objectives = Maps.newHashMap();

    @MethodTypeHint(signature = "(this: entity, lines: list[text]) -> void", documentation = "Sets the line of the sidebar of the given player.")
    public static void set_sidebar(REntity $this, RList list) {
        if(list.javaValue().isEmpty()) {
            return;
        }
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            if(!scoreboards.containsKey(serverPlayer.getUUID())) {
                var scoreboard = new Scoreboard();
                var objective = scoreboard.addObjective(
                        "polaris-sidebar",
                        ObjectiveCriteria.DUMMY,
                        (Component) list.javaValue().getFirst().javaValue(),
                        ObjectiveCriteria.RenderType.INTEGER,
                        true,
                        BlankFormat.INSTANCE
                );

                scoreboards.put(serverPlayer.getUUID(), scoreboard);
                objectives.put(serverPlayer.getUUID(), objective);

                serverPlayer.connection.send(new ClientboundSetObjectivePacket(
                        objective,
                        0
                ));
            }

            var scoreboard = scoreboards.get(serverPlayer.getUUID());
            var objective = objectives.get(serverPlayer.getUUID());

            objective.setDisplayName((Component) list.javaValue().getFirst().javaValue());

            serverPlayer.connection.send(new ClientboundSetObjectivePacket(
                    objective,
                    2
            ));
            serverPlayer.connection.send(new ClientboundSetDisplayObjectivePacket(
                    DisplaySlot.SIDEBAR,
                    objective
            ));

            for(int score = 0; score < list.javaValue().size() - 1; score++) {
                serverPlayer.connection.send(new ClientboundResetScorePacket(String.valueOf(score), "polaris-sidebar"));

                var line = list.javaValue().reversed().get(score);
                if(line instanceof RText text) {
                    serverPlayer.connection.send(new ClientboundSetScorePacket(
                            String.valueOf(score),
                            "polaris-sidebar",
                            score,
                            Optional.of(text.javaValue()),
                            Optional.of(BlankFormat.INSTANCE)
                    ));
                }
            }
        }
    }

    @MethodTypeHint(signature = "(this: entity) -> number", documentation = "Returns the selected slot in the hotbar of the entity.")
    public static RNumber selected_slot(REntity $this) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            return RNumber.of(serverPlayer.getInventory().getSelectedSlot());
        }
        return RNumber.of(0);
    }

    @MethodTypeHint(signature = "(this: entity, item: item, position: vector) -> void", documentation = "Returns the selected slot in the hotbar of the entity.")
    public static void spawn_owned_item(REntity $this, RItem item, RVector vector) {
        var ie = new ItemEntity($this.javaValue().level(), 0, 0, 0, item.javaValue());
        ie.teleportTo(vector.javaValue().x, vector.javaValue().y, vector.javaValue().z);
        $this.javaValue().level().addFreshEntity(ie);
    }

    @MethodTypeHint(signature = "(this: entity, sound: identifier) -> void", documentation = "Returns the selected slot in the hotbar of the entity.")
    public static void play_sound(REntity $this, RIdentifier sound) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            serverPlayer.level().playSound(
                    null,
                    serverPlayer.blockPosition(),
                    SoundEvent.createVariableRangeEvent(sound.javaValue()),
                    SoundSource.MASTER,
                    1f,
                    1f
            );
        }
    }
}
