package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

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

    @MethodTypeHint("(this: world, block_type: identifier, position: vector) -> void")
    public static void set_block(RWorld world, RIdentifier block, RVector vector) {
        var blockType = BuiltInRegistries.BLOCK.get(block.javaValue()).orElseThrow();
        world.javaValue().setBlock(
                vector.asBlockPos(),
                blockType.value().defaultBlockState(),
                Block.UPDATE_ALL_IMMEDIATE
        );
    }
}
