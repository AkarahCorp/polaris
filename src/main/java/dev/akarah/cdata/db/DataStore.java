package dev.akarah.cdata.db;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;

import java.util.Map;

public class DataStore {
    Map<String, Object> objects = new Object2ObjectAVLTreeMap<>();

    private DataStore() {

    }

    public static DataStore of() {
        return new DataStore();
    }

    public void put(String key, Object value) {
        this.objects.put(key, value);
    }

    public void putNumber(String key, Double number) {
        this.objects.put(key, number);
    }

    public Object get(String key, Object fallback) {
        return this.objects.getOrDefault(key, fallback);
    }

    public Double getNumber(String key, Double fallback) {
        var value = this.objects.get(key);
        if(value instanceof Double d) {
            return d;
        }
        return fallback;
    }
}
