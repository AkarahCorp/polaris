package dev.akarah.cdata.registry.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.value.RNullable;
import dev.akarah.cdata.script.value.mc.REntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record LootTable(
        List<WeightedEntry> weightedDrops,
        List<GuaranteedEntry> drops,
        IntProvider weightRolls
) {
    record GuaranteedEntry(
            ResourceLocation item,
            IntProvider amount,
            Optional<String> fortuneStat
    ) {
        public static Codec<GuaranteedEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(GuaranteedEntry::item),
                IntProvider.POSITIVE_CODEC.fieldOf("amount").forGetter(GuaranteedEntry::amount),
                Codec.STRING.optionalFieldOf("fortune_stat").forGetter(GuaranteedEntry::fortuneStat)
        ).apply(instance, GuaranteedEntry::new));
    }

    public record WeightedEntry(
            int weight,
            ResourceLocation item,
            IntProvider amount,
            Optional<String> fortuneStat
    ) {
        public static Codec<WeightedEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("weight").forGetter(WeightedEntry::weight),
                ResourceLocation.CODEC.fieldOf("item").forGetter(WeightedEntry::item),
                IntProvider.POSITIVE_CODEC.fieldOf("amount").forGetter(WeightedEntry::amount),
                Codec.STRING.optionalFieldOf("fortune_stat").forGetter(WeightedEntry::fortuneStat)
        ).apply(instance, WeightedEntry::new));
    }

    public static Codec<LootTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WeightedEntry.CODEC.listOf().optionalFieldOf("weighted_drops", List.of()).forGetter(LootTable::weightedDrops),
            GuaranteedEntry.CODEC.listOf().optionalFieldOf("drops", List.of()).forGetter(LootTable::drops),
            IntProvider.CODEC.optionalFieldOf("weighted_rolls", ConstantInt.of(1)).forGetter(LootTable::weightRolls)
    ).apply(instance, LootTable::new));

    public void execute(ServerLevel level, Vec3 position) {
        execute(level, position, null);
    }

    public void execute(ServerLevel level, Vec3 position, ServerPlayer player) {
        final RandomSource rs;
        if(player != null) {
            rs = player.getRandom();
        } else {
            rs = RandomSource.create();
        }
        for(var guaranteed : this.drops) {
            Resources.customItem().registry().get(guaranteed.item()).ifPresent(customItem -> {
                var times = 1;

                if(player != null) {
                    double stat = guaranteed.fortuneStat().map(x -> Resources.statManager().lookup(player).get(x)).orElse(0.0);

                    while(Math.random() <= stat) {
                        stat -= 1;
                        times += 1;
                    }
                }

                var generated = customItem.value().toItemStack(RNullable.of(REntity.of(player)));
                generated.setCount(guaranteed.amount().sample(rs));

                for(int i = 0; i<times; i++) {
                    var ie = new ItemEntity(level, position.x, position.y, position.z, generated);

                    if(player != null) {
                        ie.setTarget(player.getUUID());
                    }

                    level.addFreshEntity(ie);
                }
            });
        }

        int weightSum = 0;
        for(var entry : this.weightedDrops) {
            weightSum += entry.weight();
        }

        if(weightSum == 0) {
            return;
        }

        var weightRolls = this.weightRolls().sample(rs);
        for(int i = 0; i < weightRolls; i++) {
            var weightRemainder = rs.nextIntBetweenInclusive(1, weightSum);

            for(var entry : this.weightedDrops) {
                weightRemainder -= entry.weight();
                if(weightRemainder <= 0) {


                    Resources.customItem().registry().get(entry.item()).ifPresent(customItem -> {
                        var times = 1;
                        if(player != null) {
                            double stat = entry.fortuneStat().map(x -> Resources.statManager().lookup(player).get(x)).orElse(0.0);

                            while(Math.random() <= stat) {
                                stat -= 1;
                                times += 1;
                            }
                        }

                        var generated = customItem.value().toItemStack(RNullable.of(REntity.of(player)));
                        generated.setCount(entry.amount().sample(rs));

                        for(int i2 = 0; i2 < times; i2++) {
                            var ie = new ItemEntity(level, position.x, position.y, position.z, generated);
                            level.addFreshEntity(ie);
                        }
                    });
                    break;
                }
            }
        }
    }
}
