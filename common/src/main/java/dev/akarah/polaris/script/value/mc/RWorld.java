package dev.akarah.polaris.script.value.mc;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.entity.instance.DynamicEntity;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.value.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.Objects;

public class RWorld extends RuntimeValue {
    private final ServerLevel inner;

    private RWorld(ServerLevel inner) {
        this.inner = inner;
    }

    public static RWorld of(ServerLevel value) {
        return new RWorld(value);
    }



    public static String typeName() {
        return "world";
    }

    @Override
    public ServerLevel javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return RWorld.of(this.inner);
    }

    @MethodTypeHint(
            signature = "(this: world, position: vector, block_type: identifier, block_state?: dict[string, any], update?: boolean) -> void",
            documentation = "Sets a block in this world of the given type. "
            + "If no state is provided, the default state will be used."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void set_block(RWorld world, RVector vector, RIdentifier blockId, RDict stateDict, RBoolean update) {
        var block = BuiltInRegistries.BLOCK.get(blockId.javaValue()).orElseThrow().value();
        var definition = block.getStateDefinition();
        var state = block.defaultBlockState();

        if(stateDict != null) {
            for(var entry : stateDict.javaValue().entrySet()) {
                var property = definition.getProperty(entry.getKey().toString());
                if(property == null) {
                    continue;
                }
                var value = property.getValue(entry.getValue().toString().replace(".0", "")).orElse(null);
                if(value == null) {
                    continue;
                }
                state = state.setValue((Property) property, (Comparable) value);
            }
        }

        world.javaValue().setBlock(
                vector.asBlockPos(),
                state,
                Objects.requireNonNullElse(update, RBoolean.of(true)).javaValue() ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_SKIP_ALL_SIDEEFFECTS | Block.UPDATE_CLIENTS
        );
    }

    @MethodTypeHint(signature = "(this: world, position: vector) -> identifier", documentation = "Returns the block ID at the given position. If the block is empty, it will return `minecraft:air`.")
    public static RIdentifier block_at(RWorld world, RVector vector) {
        return RIdentifier.of(
                world.javaValue().getBlockState(vector.asBlockPos())
                        .getBlock().builtInRegistryHolder().key().identifier()
        );
    }


    @MethodTypeHint(
            signature = "(this: world, position: vector) -> dict[string, any]",
            documentation = "Returns the state of the block at the given position. If no block is present, it will return an empty dictionary."
    )
    public static RDict block_state_at(RWorld world, RVector vector) {
        var dict = RDict.create();
        var state = world.javaValue().getBlockState(vector.asBlockPos());
        for(var property : state.getProperties()) {
            if(property.getValueClass().equals(Boolean.class)) {
                RDict.put(dict, RString.of(property.getName()), RBoolean.of((Boolean) state.getValue(property)));
            } else if(property.getValueClass().equals(String.class)) {
                RDict.put(dict, RString.of(property.getName()), RString.of((String) state.getValue(property)));
            } else if(property.getValueClass().equals(Integer.class)) {
                RDict.put(dict, RString.of(property.getName()), RNumber.of((Integer) state.getValue(property)));
            } else {
                RDict.put(dict, RString.of(property.getName()), RString.of(state.getValue(property).toString()));
            }
        }
        return dict;
    }

    @MethodTypeHint(
            signature = "(this: world, position: vector) -> nullable[inventory]",
            documentation = "Returns the inventory present in the block at the given position. Returns null if the block is not a container."
    )
    public static RNullable block_inventory_at(RWorld world, RVector position) {
        var entity = world.javaValue().getBlockEntity(position.asBlockPos());
        if(entity instanceof Container container) {
            return RNullable.of(RInventory.of(container, RText.of(Component.literal("Container"))));
        }
        return RNullable.empty();
    }

    @MethodTypeHint(
            signature = "(this: world) -> list[entity]",
            documentation = "Returns the inventory present in the block at the given position. Returns null if the block is not a container."
    )
    public static RList entities(RWorld world) {
        var list = RList.create();

        for(var entity : world.javaValue().getEntities(EntityTypeTest.forClass(Entity.class), _ -> true)) {
            RList.add(list, REntity.of(DynamicEntity.castDynOwner(entity)));
        }

        RList.dedup(list);
        
        return list;
    }


    @MethodTypeHint(
            signature = "(this: world, uuid: uuid) -> nullable[entity]",
            documentation = "Returns the entity in the world under the given UUID if present."
    )
    public static RNullable get_entity(RWorld world, RUuid uuid) {
        return RNullable.of(REntity.of(world.javaValue().getEntity(uuid.javaValue())));
    }

    @MethodTypeHint(
            signature = "(this: world, position: vector, text: text) -> entity",
            documentation = "Returns the inventory present in the block at the given position. Returns null if the block is not a container."
    )
    public static REntity spawn_hologram(RWorld world, RVector position, RText text) {
        var textEntity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world.javaValue());

        textEntity.setText(text.javaValue());
        textEntity.teleportTo(position.javaValue().x, position.javaValue().y, position.javaValue().z);
        textEntity.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        textEntity.setTextOpacity((byte) 255);

        world.javaValue().addFreshEntity(textEntity);

        return REntity.of(textEntity);
    }



    @MethodTypeHint(signature = "(world: world, type: identifier, position: vector) -> entity", documentation = "Sets an item tag on the item, held with the key provided.")
    public static REntity spawn_entity(RWorld world, RIdentifier entity_type, RVector vector) {
        var entityBase = Resources.customEntity().registry().get(entity_type.javaValue());

        if(entityBase.isEmpty()) {
            var entityType = BuiltInRegistries.ENTITY_TYPE.get(entity_type.javaValue()).orElseThrow().value();
            var entity = entityType.create(world.javaValue(), EntitySpawnReason.COMMAND);
            assert entity != null;
            entity.teleportTo(vector.javaValue().x, vector.javaValue().y, vector.javaValue().z);
            world.javaValue().addFreshEntity(entity);

            return REntity.of(entity);
        }

        return REntity.of(entityBase.orElseThrow().value().spawn(world.javaValue(), vector.javaValue()));
    }

    @MethodTypeHint(signature = "(world: world, particle: particle, position: vector) -> void",
                    documentation = "Creates a singular particle in the position provided.")
    public static void particle__single(RWorld world, RParticle particle, RVector position) {
        world.javaValue().sendParticles(
                particle.particleOptions(),
                position.javaValue().x,
                position.javaValue().y,
                position.javaValue().z,
                1,
                0.0f,
                0.0f,
                0.0f,
                0.0f
        );
    }

    @MethodTypeHint(signature = "(world: world, particle: particle, position1: vector, position2: vector, step?: number) -> void",
            documentation = "Creates a line of particles between the two positions provided. By default, the step is half a block.")
    public static void particle__line(RWorld world, RParticle particle, RVector pos1, RVector pos2, RNumber step) {
        if (step == null) {step = RNumber.of(0.5);}
        var velocity = pos1.javaValue().subtract(pos2.javaValue()).normalize().multiply(step.doubleValue(), step.doubleValue(), step.doubleValue());
        for(int i = 0; i < pos1.javaValue().distanceTo(pos2.javaValue()) * 2; i++) {
            var position = pos1.javaValue().add(velocity.multiply(-i, -i, -i));
            world.javaValue().sendParticles(
                    particle.particleOptions(),
                    position.x,
                    position.y,
                    position.z,
                    1,
                    0.0f,
                    0.0f,
                    0.0f,
                    0.0f
            );
        }
    }

    @MethodTypeHint(signature = "(world: world, pos: vector) -> identifier", documentation = "Gets the ID of the biome at the given position.")
    public static RIdentifier biome_at(RWorld world, RVector vector) {
        return RIdentifier.of(world.javaValue().getBiome(vector.asBlockPos()).unwrapKey().orElseThrow().identifier());
    }
}
