package dev.akarah.cdata.db;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;

import java.util.Map;

public class DataStore {
    Object2ObjectAVLTreeMap<String, Object> objects;


    private DataStore(Object2ObjectAVLTreeMap<String, Object> map) {
        this.objects = map;
    }

    public static DataStore of() {
        return new DataStore(new Object2ObjectAVLTreeMap<>());
    }

    public static DataStore of(Object2ObjectAVLTreeMap<String, Object> map) {
        return new DataStore(map);
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

    public Object2ObjectAVLTreeMap<String, Object> map() {
        return this.objects;
    }
}
