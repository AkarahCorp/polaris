package dev.akarah.polaris.script.value;

import com.google.common.collect.Maps;

import java.util.Map;

public class RStruct extends RuntimeValue {
    private final String name;
    private final Map<String, RuntimeValue> inner;

    private RStruct(String name, Map<String, RuntimeValue> inner) {
        this.name = name;
        this.inner = inner;
    }

    public static RStruct create(String name, int size) {
        return new RStruct(name, Maps.newHashMap());
    }

    public static RuntimeValue get(RStruct dict, String key, RuntimeValue fallback) {
        var result = dict.inner.get(key);
        if(result == null) {
            if(fallback == null) {
                throw new RuntimeException("Field " + key + " of struct " + dict.name + " is missing.");
            }
            dict.inner.put(key, fallback);
            return fallback;
        }
        return result;
    }

    public static void put(RStruct dict, String key, RuntimeValue value) {
        dict.inner.put(key, value);
    }

    @Override
    public Map<String, RuntimeValue> javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        var copy = Maps.<String, RuntimeValue>newHashMap();
        for(var entry : this.inner.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return new RStruct(this.name, copy);
    }

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.inner.toString();
    }
}
