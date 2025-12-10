package dev.akarah.polaris.registry.stat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StatsObject {
    public static Codec<StatsObject> CODEC = Codec
            .unboundedMap(Identifier.CODEC, Codec.DOUBLE)
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
            @NotNull Identifier stat,
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

        var addMap = Maps.<@NotNull Identifier, @NotNull Double>newHashMap();
        var percMap = Maps.<@NotNull Identifier, @NotNull Double>newHashMap();
        var multMap = Maps.<@NotNull Identifier, @NotNull Double>newHashMap();
        var minMap = Maps.<@NotNull Identifier, @NotNull Double>newHashMap();
        var maxMap = Maps.<@NotNull Identifier, @NotNull Double>newHashMap();

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

    public @NotNull Map<@NotNull String, @NotNull Double> reconstructOldMap() {
        this.sortEntries();
        var map = Maps.<@NotNull String, @NotNull Double>newConcurrentMap();
        for(var source : this.performFinalCalculations().sources) {
            switch (source.operation) {
                case SourceOperation.ADD -> map.compute(source.stat.toString(), (_, value) ->
                        (value == null ? 0 : value) + source.value
                );
                case SourceOperation.PERCENTAGE -> map.compute(source.stat + "/percent", (_, value) ->
                        (value == null ? 0 : value) + source.value
                );
                case SourceOperation.MULTIPLY -> map.compute(source.stat + "/multiply", (_, value) ->
                        (value == null ? 0 : value) + source.value
                );
                case SourceOperation.MINIMUM -> map.compute(source.stat + "/min", (_, value) ->
                        (value == null ? 0 : value) + source.value
                );
                case SourceOperation.MAXIMUM -> map.compute(source.stat + "/max", (_, value) ->
                        (value == null ? 0 : value) + source.value
                );
            }
        }
        return map;
    }

    public @NotNull Map<@NotNull Identifier, @NotNull Double> values() {
        this.sortEntries();
        var map = Maps.<@NotNull Identifier, @NotNull Double>newConcurrentMap();
        for(var source : this.performFinalCalculations().sources) {
            switch (source.operation) {
                case SourceOperation.ADD -> map.compute(source.stat, (_, value) ->
                        (value == null ? 0 : value) + source.value
                );
                case SourceOperation.PERCENTAGE -> map.compute(source.stat, (_, value) ->
                        (value == null ? 0 : value) * (1 + source.value / 100)
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

    private StatsObject(Map<Identifier, Double> map) {
        for(var entry : map.entrySet()) {
            var rawKey = entry.getKey().toString().replace("/percent", "").replace("/multiply", "");
            this.sources.add(new SourceEntry(
                    Component.empty(),
                    Identifier.parse(rawKey),
                    entry.getKey().toString().endsWith("/percent") ? SourceOperation.PERCENTAGE
                    : entry.getKey().toString().endsWith("/multiply") ? SourceOperation.MULTIPLY
                    : entry.getKey().toString().endsWith("/min") ? SourceOperation.MINIMUM
                    : entry.getKey().toString().endsWith("/max") ? SourceOperation.MAXIMUM
                    : SourceOperation.ADD,
                    entry.getValue()
            ));
        }
    }

    public static StatsObject of() {
        return new StatsObject();
    }

    public boolean has(Identifier id) {
        return this.values().containsKey(id);
    }

    public double get(Identifier id) {
        return this.values().getOrDefault(id, 0.0);
    }

    public void addAllUnderName(StatsObject other, Component name) {
        for(var source : other.sources) {
            this.sources.add(new StatsObject.SourceEntry(
                    name,
                    source.stat(),
                    source.operation(),
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

    public void multiply(double multiplier) {
        var sources = Lists.<StatsObject.SourceEntry>newArrayList();

        for(var source : this.sources) {
            sources.add(new StatsObject.SourceEntry(
                    source.title(),
                    source.stat(),
                    source.operation(),
                    source.value() * multiplier
            ));
        }

        this.sources = sources;
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
