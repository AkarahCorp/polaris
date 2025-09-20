package dev.akarah.polaris.registry.loot;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.RNullable;
import dev.akarah.polaris.script.value.mc.REntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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
            Optional<ResourceLocation> fortuneStat
    ) {
        public static Codec<GuaranteedEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(GuaranteedEntry::item),
                IntProvider.POSITIVE_CODEC.fieldOf("amount").forGetter(GuaranteedEntry::amount),
                ResourceLocation.CODEC.optionalFieldOf("fortune_stat").forGetter(GuaranteedEntry::fortuneStat)
        ).apply(instance, GuaranteedEntry::new));
    }

    public record WeightedEntry(
            int weight,
            ResourceLocation item,
            IntProvider amount,
            Optional<ResourceLocation> fortuneStat
    ) {
        public static Codec<WeightedEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("weight").forGetter(WeightedEntry::weight),
                ResourceLocation.CODEC.fieldOf("item").forGetter(WeightedEntry::item),
                IntProvider.POSITIVE_CODEC.fieldOf("amount").forGetter(WeightedEntry::amount),
                ResourceLocation.CODEC.optionalFieldOf("fortune_stat").forGetter(WeightedEntry::fortuneStat)
        ).apply(instance, WeightedEntry::new));
    }

    public static Codec<LootTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WeightedEntry.CODEC.listOf().optionalFieldOf("weighted_drops", List.of()).forGetter(LootTable::weightedDrops),
            GuaranteedEntry.CODEC.listOf().optionalFieldOf("drops", List.of()).forGetter(LootTable::drops),
            IntProvider.CODEC.optionalFieldOf("weighted_rolls", ConstantInt.of(1)).forGetter(LootTable::weightRolls)
    ).apply(instance, LootTable::new));

    public List<ItemEntity> execute(ServerLevel level, Vec3 position) {
        return execute(level, position, null);
    }

    public List<ItemEntity> execute(ServerLevel level, Vec3 position, ServerPlayer player) {
        var entities = Lists.<ItemEntity>newArrayList();
        final RandomSource rs;
        if(player != null) {
            rs = player.getRandom();
        } else {
            rs = RandomSource.create();
        }
        for(var guaranteed : this.drops) {
            handleRule(level, position, player, entities, rs, guaranteed.item(), guaranteed.fortuneStat(), guaranteed.amount(), entry);
        }

        int weightSum = 0;
        for(var entry : this.weightedDrops) {
            weightSum += entry.weight();
        }

        if(weightSum == 0) {
            return entities;
        }

        var weightRolls = this.weightRolls().sample(rs);
        for(int i = 0; i < weightRolls; i++) {
            var weightRemainder = rs.nextIntBetweenInclusive(1, weightSum);

            for(var entry : this.weightedDrops) {
                weightRemainder -= entry.weight();
                if(weightRemainder <= 0) {
                    handleRule(level, position, player, entities, rs, entry.item(), entry.fortuneStat(), entry.amount(), entry);
                    break;
                }
            }
        }
        return entities;
    }

    private void handleRule(ServerLevel level, Vec3 position, ServerPlayer player, ArrayList<ItemEntity> entities, RandomSource rs, ResourceLocation item, Optional<ResourceLocation> resourceLocation, IntProvider amount, WeightedEntry entry) {
        Resources.customItem().registry().get(item).ifPresent(customItem -> {
            var times = 1;
            if(player != null) {
                double stat = resourceLocation.map(x -> Resources.statManager().lookup(player).get(x)).orElse(0.0);

                times += (int) (Math.floor(stat) + ((Math.random() <= (stat - Math.floor(stat)) ? 1 : 0)));
            }

            var generated = customItem.value().toItemStack(RNullable.of(REntity.of(player)));
            generated.setCount(amount.sample(rs) * times);

            var ie = new ItemEntity(level, position.x, position.y, position.z, generated);

            if(player != null) {
                ie.setTarget(player.getUUID());
            }

            level.addFreshEntity(ie);
            entities.add(ie);
        });
    }
}
