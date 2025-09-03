package dev.akarah.polaris.registry.stat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.Optionull;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class StatsObject {
    public static Codec<StatsObject> CODEC = Codec
            .unboundedMap(Codec.STRING, Codec.DOUBLE)
            .xmap(StatsObject::new, _ -> {
                throw new RuntimeException("Encoding statsobjects not supported.. sorry :(");
            });

    public static StatsObject EMPTY = new StatsObject();

    public enum SourceOperation {
        MINIMUM,
        MAXIMUM,
        ADD,
        PERCENTAGE,
        MULTIPLY
    }

    public record SourceEntry(
            @NotNull Component title,
            @NotNull ResourceLocation stat,
            @NotNull SourceOperation operation,
            double value
    ) {

    }

    public void sortEntries() {
        var list = Lists.<SourceEntry>newArrayList();

        for(var source : this.sources) {
            if(source.operation == SourceOperation.ADD) {
                list.add(source);
            }
        }
        for(var source : this.sources) {
            if(source.operation == SourceOperation.PERCENTAGE) {
                list.add(source);
            }
        }
        for(var source : this.sources) {
            if(source.operation == SourceOperation.MULTIPLY) {
                list.add(source);
            }
        }
        for(var source : this.sources) {
            if(source.operation == SourceOperation.MINIMUM) {
                list.add(source);
            }
        }
        for(var source : this.sources) {
            if(source.operation == SourceOperation.MAXIMUM) {
                list.add(source);
            }
        }

        this.sources = list;
    }

    public StatsObject performFinalCalculations() {
        var result = StatsObject.of();

        var addMap = Maps.<@NotNull ResourceLocation, @NotNull Double>newHashMap();
        var percMap = Maps.<@NotNull ResourceLocation, @NotNull Double>newHashMap();
        var multMap = Maps.<@NotNull ResourceLocation, @NotNull Double>newHashMap();
        var minMap = Maps.<@NotNull ResourceLocation, @NotNull Double>newHashMap();
        var maxMap = Maps.<@NotNull ResourceLocation, @NotNull Double>newHashMap();

        for (var source : this.sources) {
            var stat = source.stat();
            switch (source.operation()) {
                case ADD -> addMap.merge(stat, source.value(), Double::sum);
                case PERCENTAGE -> percMap.merge(stat, source.value(), Double::sum);
                case MULTIPLY -> multMap.merge(stat, source.value(), Double::sum);
                case MINIMUM -> minMap.merge(stat, source.value(), Double::sum);
                case MAXIMUM -> maxMap.merge(stat, source.value(), Double::sum);
            }
        }

        addMap.forEach((stat, value) ->
                result.add(new SourceEntry(Component.empty(), stat, SourceOperation.ADD, value)));
        percMap.forEach((stat, value) ->
                result.add(new SourceEntry(Component.empty(), stat, SourceOperation.PERCENTAGE, value)));
        multMap.forEach((stat, value) ->
                result.add(new SourceEntry(Component.empty(), stat, SourceOperation.MULTIPLY, value)));
        minMap.forEach((stat, value) ->
                result.add(new SourceEntry(Component.empty(), stat, SourceOperation.MINIMUM, value)));
        maxMap.forEach((stat, value) ->
                result.add(new SourceEntry(Component.empty(), stat, SourceOperation.MAXIMUM, value)));

        result.sortEntries();
        return result;
    }

    public @NotNull Map<@NotNull ResourceLocation, @NotNull Double> values() {
        this.sortEntries();
        var map = Maps.<@NotNull ResourceLocation, @NotNull Double>newConcurrentMap();
        for(var source : this.performFinalCalculations().sources) {
            switch (source.operation) {
                case SourceOperation.ADD -> map.compute(source.stat, (_, value) ->
                        (value == null ? 0 : value) + source.value
                );
                case SourceOperation.MULTIPLY -> map.compute(source.stat, (_, value) ->
                        (value == null ? 0 : value) * (1 + source.value)
                );
                case SourceOperation.MINIMUM -> map.compute(source.stat, (_, value) ->
                        (value == null ? 0 : value) * Math.max((value == null ? 0 : value), source.value)
                );
                case SourceOperation.MAXIMUM -> map.compute(source.stat, (_, value) ->
                        (value == null ? 0 : value) * Math.min((value == null ? 0 : value), source.value)
                );
            }
        }
        return map;
    }


    public List<SourceEntry> sources = Collections.synchronizedList(Lists.newArrayList());

    private StatsObject() {

    }

    private StatsObject(List<SourceEntry> map) {
        this.sources = map;
    }

    private StatsObject(Map<String, Double> map) {
        for(var entry : map.entrySet()) {
            var rawKey = entry.getKey().replace("%", "").replace("*", "");
            this.sources.add(new SourceEntry(
                    Component.empty(),
                    ResourceLocation.parse(rawKey),
                    entry.getKey().endsWith("%") ? SourceOperation.PERCENTAGE
                    : entry.getKey().endsWith("*") ? SourceOperation.MULTIPLY
                    : SourceOperation.ADD,
                    entry.getValue()
            ));
        }
    }

    public static StatsObject of() {
        return new StatsObject();
    }

    public boolean has(ResourceLocation id) {
        return this.values().containsKey(id);
    }

    public double get(ResourceLocation id) {
        return this.values().getOrDefault(id, 0.0);
    }

    public void addAllUnderName(StatsObject other, Component name) {
        for(var source : other.sources) {
            this.sources.add(new StatsObject.SourceEntry(
                    name,
                    source.stat(),
                    StatsObject.SourceOperation.ADD,
                    source.value()
            ));
        }
    }

    public void add(StatsObject other) {
        this.sources.addAll(other.sources);
    }

    public void add(SourceEntry other) {
        this.sources.add(other);
    }

    public StatsObject copy() {
        var so = StatsObject.of();
        so.add(this);
        return so;
    }

    public StatsObject withRenamedSources(Component newName) {
        var so = StatsObject.of();
        for(var source : this.sources) {
            so.add(new StatsObject.SourceEntry(
                    newName,
                    source.stat(),
                    source.operation(),
                    source.value()
            ));
        }
        return so;
    }

    @Override
    public @NotNull String toString() {
        return this.sources.toString();
    }
}
