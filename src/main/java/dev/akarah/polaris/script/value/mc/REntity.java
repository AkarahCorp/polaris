package dev.akarah.polaris.script.value.mc;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.db.Database;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.entity.instance.DynamicEntity;
import dev.akarah.polaris.registry.stat.StatsObject;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.value.*;
import dev.akarah.polaris.script.value.polaris.DynamicContainer;
import dev.akarah.polaris.script.value.polaris.DynamicContainerMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class REntity extends RuntimeValue {
    private final Entity inner;

    private REntity(Entity inner) {
        this.inner = inner;
    }

    public static REntity of(Entity value) {
        if(value == null) {
            return null;
        }
        return new REntity(value);
    }

    @Override
    public Entity javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return REntity.of(this.inner);
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

    @MethodTypeHint(signature = "(this: entity) -> number", documentation = "Returns the direction of this entity, in the form of an X/Y/Z vector.")
    public static RNumber pitch(REntity $this) {
        return RNumber.of($this.inner.getYRot());
    }

    @MethodTypeHint(signature = "(this: entity) -> number", documentation = "Returns the direction of this entity, in the form of an X/Y/Z vector.")
    public static RNumber yaw(REntity $this) {
        return RNumber.of($this.inner.getXRot());
    }

    @MethodTypeHint(signature = "(this: entity, pitch: number, yaw: number) -> void", documentation = "Returns the direction of this entity, in the form of an X/Y/Z vector.")
    public static void set_direction(REntity $this, RNumber pitch, RNumber yaw) {
        $this.inner.setXRot(yaw.javaValue().floatValue());
        $this.inner.setYRot(pitch.javaValue().floatValue());
    }

    @MethodTypeHint(signature = "(this: entity, time: number) -> void", documentation = "Returns the direction of this entity, in the form of an X/Y/Z vector.")
    public static void set_invulnerable_time(REntity $this, RNumber time) {
        $this.javaValue().invulnerableTime = time.intValue();
    }

    @MethodTypeHint(signature = "(this: entity, position: vector) -> void", documentation = "Teleports the entity to the given position.")
    public static void teleport(REntity $this, RVector vector) {
        $this.inner.teleportTo(vector.javaValue().x, vector.javaValue().y, vector.javaValue().z);
    }

    @MethodTypeHint(signature = "(this: entity, message: text) -> void", documentation = "Sends a message in the chat of the player provided.")
    public static void player__send_message(REntity $this, RText message) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message.javaValue());
        }
    }

    @MethodTypeHint(signature = "(this: entity, message: text) -> void", documentation = "Sends a message through the actionbar to the player provided.")
    public static void player__send_actionbar(REntity $this, RText message) {
        if($this.inner instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message.javaValue(), true);
        }
    }

    @MethodTypeHint(signature = "(this: entity) -> string", documentation = "Gets the name of the provided entity.")
    public static RString name(REntity $this) {
        if($this.inner instanceof DynamicEntity dynamicEntity) {
            return RString.of(dynamicEntity.base().name());
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

    @MethodTypeHint(signature = "(this: entity, health: number, type?: identifier, attacker?: entity) -> void", documentation = "Sets the health of the entity.")
    public static void damage(REntity $this, RNumber number, RIdentifier type, REntity attacker) {
        if(type == null) {
            type = RIdentifier.of(ResourceLocation.withDefaultNamespace("generic"));
        }
        if(attacker == null) {
            attacker = new REntity(null);
        }
        if($this.inner instanceof LivingEntity le) {
            var ds = new DamageSource(
                    le.level().registryAccess().lookup(Registries.DAMAGE_TYPE)
                            .orElseThrow()
                            .get(type.javaValue())
                            .orElseThrow(),
                    attacker.javaValue()
            );
            le.hurtServer((ServerLevel) le.level(), ds, number.javaValue().floatValue());
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
    public static RNullable player__current_open_inventory(REntity $this) {
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
    public static void player__open_inventory(REntity $this, RInventory inventory) {
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

    @MethodTypeHint(signature = "(this: entity, stat: identifier) -> number", documentation = "Gets the stat key for the given player, returning 0.0 as a default.")
    public static RNumber stat(REntity $this, RIdentifier key) {
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
                    attr = dynamicEntity.wrappedEntity.getAttribute(attributeReference);
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

    @MethodTypeHint(signature = "(this: entity, stats: stat_obj) -> void", documentation = "Adds the stats in the stat object to a player entity.")
    public static void player__add_stats(REntity $this, RStatsObject statsObject) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            var so = Resources.statManager().lookup(serverPlayer);
            so.add(statsObject.javaValue());
            Resources.statManager().set(serverPlayer, so);
        }
    }

    @MethodTypeHint(signature = "(this: entity) -> nullable[uuid]", documentation = "Returns the owner of the entity if it is an ownable entity. Could still be null if the ownable entity has no owner.")
    public static RNullable owner(REntity $this) {
        if($this.javaValue() instanceof ItemEntity item) {
            if(item.target == null) {
                return RNullable.empty();
            }
            return RNullable.of(RUuid.of(item.target));
        }
        if($this.javaValue() instanceof Projectile projectile) {
            if(projectile.getOwner() == null) {
                return RNullable.empty();
            }
            return RNullable.of(RUuid.of(projectile.getOwner().getUUID()));
        }
        return RNullable.empty();
    }


    @MethodTypeHint(signature = "(this: entity) -> nullable[uuid]", documentation = "Returns the owner of the entity if it is an ownable entity. Could still be null if the ownable entity has no owner.")
    public static RNullable item__owner(REntity $this) {
        return owner($this);
    }

    @MethodTypeHint(signature = "(this: entity, owner: uuid) -> void", documentation = "Sets the owner of the entity if it is an ownable entity.")
    public static void set_owner(REntity $this, RUuid owner) {
        if($this.javaValue() instanceof ItemEntity item) {
            item.target = owner.javaValue();
        }
        if($this.javaValue() instanceof Projectile projectile) {
            projectile.setOwner(Main.server().overworld().getEntity(owner.javaValue()));
        }
    }

    @MethodTypeHint(signature = "(this: entity) -> nullable[item]", documentation = "Returns the item of this entity if it is an item entity.")
    public static RNullable item__get_item(REntity $this) {
        if($this.javaValue() instanceof ItemEntity item) {
            return RNullable.of(RItem.of(item.getItem()));
        }
        return RNullable.empty();
    }

    @MethodTypeHint(signature = "(this: entity) -> void", documentation = "Removes the entity.")
    public static void remove(REntity $this) {
        $this.javaValue().remove(Entity.RemovalReason.KILLED);
    }

    public static Map<UUID, Scoreboard> scoreboards = Maps.newHashMap();
    public static Map<UUID, Objective> objectives = Maps.newHashMap();

    @MethodTypeHint(signature = "(this: entity, lines: list[text]) -> void", documentation = "Sets the lines of the sidebar of the given player.")
    public static void player__set_sidebar(REntity $this, RList list) {
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

            for(int score = 0; score < 48; score++) {
                serverPlayer.connection.send(new ClientboundResetScorePacket(
                        String.valueOf(score),
                        "polaris-sidebar"
                ));
            }

            for(int score = 0; score < (list.javaValue().size() - 1); score++) {
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
    public static RNumber player__selected_slot(REntity $this) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            return RNumber.of(serverPlayer.getInventory().getSelectedSlot());
        }
        return RNumber.of(0);
    }

    @MethodTypeHint(signature = "(this: entity, item: item, position: vector) -> void", documentation = "Creates a new item entity owned by this entity, with specified item and position.")
    public static void player__spawn_owned_item(REntity $this, RItem item, RVector vector) {
        var ie = new ItemEntity($this.javaValue().level(), 0, 0, 0, item.javaValue());
        ie.teleportTo(vector.javaValue().x, vector.javaValue().y, vector.javaValue().z);
        $this.javaValue().level().addFreshEntity(ie);
    }

    @MethodTypeHint(signature = "(this: entity, sound: identifier, pitch?: number, volume?: number) -> void", documentation = "Play a sound to a player.")
    public static void player__play_sound(REntity $this, RIdentifier sound, RNumber pitch, RNumber volume) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            serverPlayer.level().playSound(
                    null,
                    serverPlayer.blockPosition(),
                    SoundEvent.createVariableRangeEvent(sound.javaValue()),
                    SoundSource.MASTER,
                    volume == null ? 1.0f : volume.javaValue().floatValue(),
                    pitch == null ? 1.0f : pitch.javaValue().floatValue()
            );
        }
    }

    @MethodTypeHint(signature = "(this: entity, velocity: vector) -> void", documentation = "Sets the velocity of the player to the velocity provided.")
    public static void set_velocity(REntity $this, RVector velocity) {
        $this.javaValue().setDeltaMovement(velocity.javaValue());
        $this.javaValue().hurtMarked = true;
    }

    @MethodTypeHint(signature = "(this: entity, item: item) -> void", documentation = "Returns the selected slot in the hotbar of the entity.")
    public static void player__set_cursor_item(REntity $this, RItem item) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.setCarried(item.javaValue());
            serverPlayer.containerMenu.sendAllDataToRemote();
        }
    }

    @MethodTypeHint(signature = "(this: entity) -> item", documentation = "Get the item on the player's inventory cursor, if present.")
    public static RItem player__cursor_item(REntity $this) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            return RItem.of(serverPlayer.containerMenu.getCarried());
        }
        return RItem.of(ItemStack.EMPTY);
    }

    @MethodTypeHint(signature = "(entity: entity, key: string) -> nullable[any]", documentation = "Gets a custom item tag from the item, based on the key provided.")
    public static RNullable tag(REntity $this, RString keyTag) {
        return RNullable.of(
                Optional.<RuntimeValue>empty()
                        .or(() -> Optional.ofNullable($this.javaValue().get(DataComponents.CUSTOM_DATA))
                                .flatMap(x -> Optional.ofNullable(x.copyTag().get(keyTag.javaValue())))
                                .flatMap(x -> RuntimeValue.CODEC.decode(NbtOps.INSTANCE, x).result().map(Pair::getFirst)))
                        .orElse(null)
        );
    }

    @MethodTypeHint(signature = "(entity: entity, key: string, value: any) -> void", documentation = "Sets an item tag on the item, held with the key provided.")
    public static void set_tag(REntity $this, RString keyTag, RuntimeValue keyValue) {
        if($this.javaValue().get(DataComponents.CUSTOM_DATA) == null) {
            $this.javaValue().setComponent(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
        }

        $this.javaValue().setComponent(
                DataComponents.CUSTOM_DATA,
                Objects.requireNonNull($this.javaValue().get(DataComponents.CUSTOM_DATA)).update(tag -> tag.put(
                        keyTag.javaValue(),
                        RuntimeValue.CODEC.encodeStart(NbtOps.INSTANCE, keyValue).result().orElse(DoubleTag.valueOf(0.0))
                ))
        );
    }

    @MethodTypeHint(signature = "(entity: entity, text: text) -> void", documentation = "Sets the text on the provided Text Display.")
    public static void display__set_text(REntity $this, RText text) {
        if($this.javaValue() instanceof Display.TextDisplay textDisplay) {
            textDisplay.setText(text.javaValue());
        }
    }

    @MethodTypeHint(signature = "(entity: entity, scale: vector) -> void", documentation = "Sets the text on the provided Text Display.")
    public static void display__set_scale(REntity $this, RVector scale) {
        if($this.javaValue() instanceof Display display) {
            var oldTransformation = Display.createTransformation(display.getEntityData());
            display.setTransformation(
                    new Transformation(
                            oldTransformation.getTranslation(),
                            oldTransformation.getRightRotation(),
                            scale.javaValue().toVector3f(),
                            oldTransformation.getLeftRotation()
                    )
            );
        }
    }

    @MethodTypeHint(signature = "(entity: entity, rotation: vector) -> void", documentation = "Sets the rotation on the provided display entity.")
    public static void display__set_left_rotation(REntity $this, RVector scale) {
        if($this.javaValue() instanceof Display display) {
            var oldTransformation = Display.createTransformation(display.getEntityData());
            display.setTransformation(
                    new Transformation(
                            oldTransformation.getTranslation(),
                            oldTransformation.getRightRotation(),
                            oldTransformation.getScale(),
                            scale.asRotation()
                    )
            );
        }
    }

    @MethodTypeHint(signature = "(entity: entity, rotation: vector) -> void", documentation = "Sets the rotation on the provided display entity.")
    public static void display__set_right_rotation(REntity $this, RVector scale) {
        if($this.javaValue() instanceof Display display) {
            var oldTransformation = Display.createTransformation(display.getEntityData());
            display.setTransformation(
                    new Transformation(
                            oldTransformation.getTranslation(),
                            scale.asRotation(),
                            oldTransformation.getScale(),
                            oldTransformation.getLeftRotation()
                    )
            );
        }
    }



    @MethodTypeHint(signature = "(entity: entity, dialog: identifier) -> void", documentation = "Shows a dialog to the player.")
    public static void player__show_dialog(REntity $this, RIdentifier dialogId) {
        if($this.javaValue() instanceof ServerPlayer player) {
            try {
                var dialog = player.level().registryAccess().lookup(Registries.DIALOG).orElseThrow()
                        .get(dialogId.javaValue()).orElseThrow();
                player.openDialog(dialog);
            } catch (Exception ignored) {

            }
        }
    }

    @MethodTypeHint(signature = "(entity: entity) -> boolean", documentation = "Returns true if the entity still exists in the world.")
    public static RBoolean exists(REntity $this) {
        return RBoolean.of(!$this.javaValue().isRemoved());
    }

    @MethodTypeHint(signature = "(entity: entity) -> boolean", documentation = "Returns true if the player is sneaking.")
    public static RBoolean player__sneaking(REntity $this) {
        return RBoolean.of($this.javaValue() instanceof ServerPlayer serverPlayer && serverPlayer.isCrouching());
    }

    @MethodTypeHint(signature = "(entity: entity, position: vector) -> boolean", documentation = "Returns true if the position is in the entity's hitbox.")
    public static RBoolean position_in_hitbox(REntity $this, RVector position) {
        return RBoolean.of(
                $this.javaValue()
                        .getBoundingBox()
                        .contains(position.javaValue())
        );
    }

    @MethodTypeHint(signature = "(entity: entity, fuse: number) -> void", documentation = "Returns true if the position is in the entity's hitbox.")
    public static void tnt__set_fuse(REntity $this, RNumber fuse) {
        if($this.javaValue() instanceof PrimedTnt tnt) {
            tnt.setFuse(fuse.intValue());
        }
    }

    @MethodTypeHint(signature = "(entity: entity, advancements: list[identifier]) -> void", documentation = "Returns true if the position is in the entity's hitbox.")
    public static void player__set_advancements(REntity $this, RList list) {
        if($this.javaValue() instanceof ServerPlayer serverPlayer) {
            for(var adv : Main.server().getAdvancements().getAllAdvancements()) {
                var prog = serverPlayer.getAdvancements().getOrStartProgress(adv);

                if(list.javaValue().contains(RIdentifier.of(adv.id()))) {
                    for(var criteria : prog.getRemainingCriteria()) {
                        serverPlayer.getAdvancements().award(adv, criteria);
                    }
                } else {
                    for(var completed : prog.getCompletedCriteria()) {
                        serverPlayer.getAdvancements().revoke(adv, completed);
                    }
                }
            }

        }
    }
}
