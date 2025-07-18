package dev.akarah.cdata.script.value.mc;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.RBoolean;
import dev.akarah.cdata.script.value.RDict;
import dev.akarah.cdata.script.value.RNullable;
import dev.akarah.cdata.script.value.RNumber;
import dev.akarah.cdata.script.value.RString;
import dev.akarah.cdata.script.value.RuntimeValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class RWorld extends RuntimeValue {
    private final ServerLevel inner;

    private RWorld(ServerLevel inner) {
        this.inner = inner;
    }

    public static RWorld of(ServerLevel value) {
        return new RWorld(value);
    }

    @Override
    public ServerLevel javaValue() {
        return this.inner;
    }

    @MethodTypeHint(
            signature = "(this: world, position: vector, block_type: identifier, block_state?: dict[string, any]) -> void",
            documentation = "Sets a block in this world of the given type. " +
                    "If no state value is provided, it will use the default block state."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void set_block(RWorld world, RVector vector, RIdentifier blockId, RDict stateDict) {
        var block = BuiltInRegistries.BLOCK.get(blockId.javaValue()).orElseThrow().value();
        var definition = block.getStateDefinition();
        var state = block.defaultBlockState();

        if(stateDict != null) {
            for(var entry : stateDict.javaValue().entrySet()) {
                var property = definition.getProperty((String) entry.getKey().javaValue());
                assert property != null;
                var value = property.getValue(entry.getValue().toString()).orElseThrow();
                state = state.setValue((Property) property, (Comparable) value);
            }
        }

        world.javaValue().setBlock(
                vector.asBlockPos(),
                state,
                Block.UPDATE_ALL_IMMEDIATE
        );
    }

    @MethodTypeHint(signature = "(this: world, position: vector) -> identifier", documentation = "Returns the block ID at the given position. If the block is empty, it will return `minecraft:air`.")
    public static RIdentifier block_at(RWorld world, RVector vector) {
        return RIdentifier.of(
                world.javaValue().getBlockState(vector.asBlockPos())
                        .getBlock().builtInRegistryHolder().key().location()
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
            return RNullable.of(RInventory.of(container));
        }
        return RNullable.empty();
    }
}
