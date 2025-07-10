package dev.akarah.cdata.script.expr.world;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.akarah.cdata.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldUtil {
    public static Level getLevel(ResourceLocation resourceLocation) {
        return Main.server().getLevel(
                Main.server().registryAccess()
                        .getOrThrow(Registries.DIMENSION).value()
                        .get(resourceLocation).orElseThrow().value().dimension()
        );
    }

    public static void setBlock(Level level, Vec3 pos, ResourceLocation blockId, Map<String, String> blockStateMap) {
        var block = BuiltInRegistries.BLOCK.get(blockId)
                .map(Holder.Reference::value)
                .orElse(Blocks.AIR);

        var blockState = block.defaultBlockState();
        var blockPos = new BlockPos((int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z));

        level.setBlock(
                blockPos,
                blockState,
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SKIP_ON_PLACE | Block.UPDATE_ALL
        );
    }

    public static ArrayList<Entity> entities(Level level) {
        if(level instanceof ServerLevel serverLevel) {
            return Lists.newArrayList(serverLevel.getAllEntities());
        }
        return Lists.newArrayList();
    }
}
