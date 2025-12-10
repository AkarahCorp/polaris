package dev.akarah.polaris.registry.mining;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.RNumber;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RIdentifier;
import dev.akarah.polaris.script.value.mc.RVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class MiningManager {
    Map<UUID, MiningStatus> statuses = Maps.newHashMap();

    public record MiningStatus(
            BlockPos target,
            MiningRule appliedRule,
            AtomicDouble progress
    ) {

    }

    public Optional<MiningRule> ruleForMaterial(BlockState state, BlockPos position) {
        return Resources.miningRule().registry().listElements()
                .map(Holder.Reference::value)
                .filter(x -> x.materials().contains(state.getBlock()))
                .filter(x -> {
                    var definition = state.getBlock().getStateDefinition();
                    for(var stateEntry : x.stateRequirements().entrySet()) {
                        try {
                            var property = definition.getProperty(stateEntry.getKey());
                            assert property != null;
                            var value = property.getValue(stateEntry.getValue()).orElseThrow();
                            if(!state.getValue(property).equals(value)) {
                                return false;
                            }
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(x -> {
                    var minPos = x.area().getFirst();
                    var maxPos = x.area().getSecond();
                    return position.getX() >= minPos.getX()
                            && position.getY() >= minPos.getY()
                            && position.getZ() >= minPos.getZ()
                            && position.getX() <= maxPos.getX()
                            && position.getY() <= maxPos.getY()
                            && position.getZ() <= maxPos.getZ();
                })
                .findFirst();
    }

    public Optional<MiningStatus> statusFor(ServerPlayer serverPlayer) {
        return Optional.ofNullable(statuses.get(serverPlayer.getUUID()));
    }

    public void setStatus(ServerPlayer player, MiningStatus status) {
        statuses.put(player.getUUID(), status);
    }

    public void clearStatus(ServerPlayer player) {
        statusFor(player).ifPresent(status -> {
            player.connection.send(
                    new ClientboundBlockDestructionPacket(
                            player.getId() + 1,
                            status.target,
                            -1
                    )
            );
        });
        statuses.remove(player.getUUID());
    }

    public void tickPlayers() {
        for(var player : Main.server().getPlayerList().getPlayers()) {
            statusFor(player).ifPresent(status -> {
                var miningSpeed = Resources.statManager().lookup(player).get(status.appliedRule().speedStat());
                var currentTicks = status.progress().addAndGet(1);
                var targetTicks = (30 * status.appliedRule().toughness()) / miningSpeed;

                if(Math.floor(currentTicks) % 4 == 1) {
                    var block = player.level().getBlockState(status.target());
                    player.level().playSound(
                            null,
                            status.target,
                            block.getSoundType().getHitSound(),
                            SoundSource.BLOCKS,
                            1f,
                            1f
                    );
                }

                if(currentTicks > targetTicks) {
                    var spread = Resources.statManager().lookup(player).get(status.appliedRule().spreadStat());
                    var toughness = status.appliedRule().spreadToughness();

                    if(status.appliedRule().spreadToughness() > 0.0) {
                        double finalSpread = spread;
                        spread = status.appliedRule().spreadToughnessFunction().map(
                            function -> {
                                try {
                                    return ((RNumber) Resources.actionManager().methodHandleByLocation(function).invoke(RNumber.of(toughness), RNumber.of(finalSpread))).doubleValue();
                                } catch (Throwable e) {
                                    throw new RuntimeException(e);
                                }
                            }).orElse( Math.sqrt(spread) / (status.appliedRule().spreadToughness() / 100));
                    }

                    recursiveBreaking(player, status.appliedRule(), spread, status.target(), (int) targetTicks);
                    clearStatus(player);
                } else {
                    player.connection.send(
                            new ClientboundBlockDestructionPacket(
                                    player.getId() + 1,
                                    status.target(),
                                    (int) (currentTicks / targetTicks * 10)
                            )
                    );
                }
            });
        }
    }

    public static List<BlockPos> VECTORS_INNER = null;

    public static List<BlockPos> vectors() {
        if(VECTORS_INNER != null) {
            return VECTORS_INNER;
        }

        var list = Lists.<BlockPos>newArrayList();

        for(int x = -1; x<=1; x++) {
            for(int y = -1; y<=1; y++) {
                for(int z = -1; z<=1; z++) {
                    list.add(BlockPos.ZERO.offset(x, y, z));
                }
            }
        }

        VECTORS_INNER = list;
        return VECTORS_INNER;
    }




    public void recursiveBreaking(ServerPlayer player, MiningRule rule, double remainingSpread, BlockPos target, int tickTime) {
        recursiveBreakingInner(player, rule, remainingSpread, target, 0, tickTime, Lists.newArrayList());
    }

    public void recursiveBreakingInner(ServerPlayer player, MiningRule rule, double remainingSpread, BlockPos target, int depth, int tickTime, List<BlockPos> minedBlocks) {
        if(!rule.materials().contains(player.level().getBlockState(target).getBlock())) {
            return;
        }

        Resources.scheduler().schedule(
                depth,
                () -> {
                    var block = player.level().getBlockState(target);
                    if(block.getBlock().equals(Blocks.AIR)) {
                        return;
                    }
                    if(rule.regeneration().isPresent() && block.getBlock().equals(rule.regeneration().get().replacement())) {
                        return;
                    }

                    var result = Resources.actionManager().performEvents(
                            "player.break_block",
                            REntity.of(player),
                            RVector.of(target.getCenter())
                    );

                    if(!result) {
                        return;
                    }



                    var result2 = Resources.actionManager().performEvents(
                            "player.mining_rule_activation",
                            REntity.of(player),
                            RVector.of(target.getCenter()),
                            RIdentifier.of(Resources.miningRule().registry().getKey(rule))
                    );

                    if(!result2) {
                        return;
                    }

                    player.level().playSound(
                            null,
                            target,
                            block.getSoundType().getBreakSound(),
                            SoundSource.BLOCKS,
                            1f,
                            1f
                    );
                    if(player.level().getBlockEntity(target) instanceof Container container) {
                        for (var itemStack : container) {
                            var ee = new ItemEntity(player.level(), target.getX(), target.getY(), target.getZ(), itemStack);
                            ee.setTarget(player.getUUID());
                            player.level().addFreshEntity(ee);
                        }
                    }

                    rule.regeneration().ifPresentOrElse(
                            regenerationRule -> {
                                player.level().setBlock(target, regenerationRule.replacement().defaultBlockState(), Block.UPDATE_SKIP_ALL_SIDEEFFECTS | Block.UPDATE_CLIENTS);
                                Resources.scheduler().schedule(regenerationRule.delay(), () -> {
                                    player.level().setBlock(target, block, Block.UPDATE_SKIP_ALL_SIDEEFFECTS | Block.UPDATE_CLIENTS);
                                });
                            },
                            () -> {
                                player.level().setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_SKIP_ALL_SIDEEFFECTS | Block.UPDATE_CLIENTS);
                            });
                    rule.lootTable().ifPresent(x -> x.execute(player.level(), target.getCenter(), player));
                }
        );

        Collections.shuffle(vectors());

        var nearbyIsSafe = new ArrayList<BlockPos>();
        for(var vector : vectors()) {
            var randomAdjacent = target.offset(vector);
            if(
                    !minedBlocks.contains(randomAdjacent)
                            && rule.materials().contains(player.level().getBlockState(randomAdjacent).getBlock())
                            && ruleForMaterial(player.level().getBlockState(randomAdjacent), randomAdjacent).map(x -> x.equals(rule)).orElse(false)
                            && Math.random() < remainingSpread
            ) {
                remainingSpread -= 1;
                nearbyIsSafe.add(randomAdjacent);
                minedBlocks.add(randomAdjacent);
            }
        }

        for(var safe : nearbyIsSafe) {
            recursiveBreakingInner(player, rule, remainingSpread, safe, depth + 1, tickTime, minedBlocks);
        }
    }
}
