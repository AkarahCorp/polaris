package dev.akarah.cdata.registry.stat;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import dev.akarah.cdata.EngineConfig;
import dev.akarah.cdata.Main;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StatsObject {
    public static Codec<StatsObject> CODEC = Codec
            .unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE)
            .xmap(StatsObject::new, x -> x.values);

    public static StatsObject EMPTY = new StatsObject();

    Map<ResourceLocation, Double> values = new HashMap<>();

    private StatsObject() {

    }

    private StatsObject(Map<ResourceLocation, Double> map) {
        this.values = map;
    }

    public static StatsObject of() {
        return new StatsObject();
    }

    public boolean has(ResourceLocation id) {
        return this.values.containsKey(id);
    }

    public void set(ResourceLocation id, double value) {
        this.values.put(id, value);
    }

    public double get(ResourceLocation id) {
        return this.values.getOrDefault(id, 0.0);
    }

    public Set<ResourceLocation> keySet() {
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
        var so = Main.config().baseStats().copy();
        for(var key : this.keySet()) {
            so.set(key, this.get(key));
        }
        for(var key : this.keySet()) {
            if(key.getPath().endsWith("/pct")) {
                so.set(key, this.get(key) * this.get(key.withPath(x -> x.replace("/pct", ""))));
            } else if(key.getPath().endsWith("/mul")) {
                so.set(key, this.get(key) * this.get(key.withPath(x -> x.replace("/mul", ""))));
            }
        }
        return so;
    }

    @Override
    public @NotNull String toString() {
        return this.values.toString();
    }
}
