package dev.akarah.cdata.script.value.mc;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.*;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.Property;

public class RWorld extends RuntimeValue<ServerLevel> {
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

    @MethodTypeHint("(this: world, position: vector, block_type: identifier, block_state: dict[string, any]?) -> void")
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

    @MethodTypeHint("(this: world, position: vector) -> identifier")
    public static RIdentifier get_block(RWorld world, RVector vector) {
        return RIdentifier.of(
                world.javaValue().getBlockState(vector.asBlockPos())
                        .getBlock().builtInRegistryHolder().key().location()
        );
    }


    @MethodTypeHint("(this: world, position: vector) -> dict[string, any]")
    public static RDict get_block_state(RWorld world, RVector vector) {
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
}
