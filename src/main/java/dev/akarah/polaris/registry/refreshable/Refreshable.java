package dev.akarah.polaris.registry.refreshable;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public record Refreshable(
        Identifier world,
        List<Identifier> structures,
        List<BlockPos> positions,
        int frequency
) {
    public static Codec<Refreshable> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("world").forGetter(Refreshable::world),
            Identifier.CODEC.listOf().fieldOf("structures").forGetter(Refreshable::structures),
            BlockPos.CODEC.listOf().fieldOf("positions").forGetter(Refreshable::positions),
            Codec.INT.fieldOf("frequency").forGetter(Refreshable::frequency)
    ).apply(instance, Refreshable::new)));

    public void execute() {
        try {
            if(Main.server() == null) {
                return;
            }
            var world = Main.server().getLevel(ResourceKey.create(Registries.DIMENSION, this.world));
            assert world != null;

            var structures = Lists.<StructureTemplate>newArrayList();
            for(var structure : this.structures) {
                structures.add(world.getStructureManager().get(structure).orElseThrow());
            }
            if(Main.server().getTickCount() % frequency == 5) {
                var structure = structures.get((int) (Math.random() * (structures.size() - 1)));
                for(var position : this.positions) {
                    var half = new BlockPos(
                            structure.getSize().getX() / -2,
                            structure.getSize().getY() / -2,
                            structure.getSize().getZ() / -2
                    );
                    structure.placeInWorld(
                            world,
                            position.offset(half.atY(0)),
                            half,
                            new StructurePlaceSettings(),
                            world.random,
                            Block.UPDATE_ALL_IMMEDIATE
                    );
                }
            }
        } catch (Exception e) {

        }
    }
}
