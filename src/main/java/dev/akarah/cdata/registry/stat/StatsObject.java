package dev.akarah.cdata.registry.stat;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StatsObject {
    public static Codec<StatsObject> CODEC = Codec
            .unboundedMap(Codec.STRING, Codec.DOUBLE)
            .xmap(StatsObject::new, x -> x.values);

    public static StatsObject EMPTY = new StatsObject();

    Map<String, Double> values = new HashMap<>();

    private StatsObject() {

    }

    private StatsObject(Map<String, Double> map) {
        this.values = map;
    }

    public static StatsObject of() {
        return new StatsObject();
    }

    public boolean has(ResourceLocation id) {
        return this.values.containsKey(id.toString());
    }

    public void set(ResourceLocation id, double value) {
        this.values.put(id.toString(), value);
    }

    public double get(ResourceLocation id) {
        return this.values.getOrDefault(id.toString(), 0.0);
    }

    public boolean has(String id) {
        return this.values.containsKey(id);
    }

    public void set(String id, double value) {
        this.values.put(id, value);
    }

    public double get(String id) {
        return this.values.getOrDefault(id, 0.0);
    }

    public Set<String> keySet() {
        return this.values.keySet();
    }

    public void add(StatsObject other) {
        var keys = Sets.union(this.keySet(), other.keySet());
        for(var key : keys) {
            this.set(key, this.get(key) + other.get(key));
        }
    }

    public StatsObject copy() {
        var so = StatsObject.of();
        for(var entry : this.values.entrySet()) {
            so.set(entry.getKey(), entry.getValue());
        }
        return so;
    }

    public StatsObject performFinalCalculations() {
        for(var key : this.keySet()) {
            var realKey = key.replace("%", "").replace("*", "");
            if(key.endsWith("%")) {
                this.set(realKey, ((this.get(key) + 100) / 100) * this.get(realKey));
            } else if(key.endsWith("*")) {
                this.set(realKey, (this.get(key) + 1.0) * this.get(realKey));
            }
        }
        return this;
    }

    @Override
    public @NotNull String toString() {
        return this.values.toString();
    }
}
