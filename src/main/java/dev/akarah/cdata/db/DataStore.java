package dev.akarah.cdata.db;

import dev.akarah.cdata.script.value.RNumber;
import dev.akarah.cdata.script.value.RuntimeValue;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;

import java.util.Map;

public class DataStore {
    Object2ObjectAVLTreeMap<String, RuntimeValue> objects;


    private DataStore(Object2ObjectAVLTreeMap<String, RuntimeValue> map) {
        this.objects = map;
    }

    public static DataStore of() {
        return new DataStore(new Object2ObjectAVLTreeMap<>());
    }

    public static DataStore of(Object2ObjectAVLTreeMap<String, RuntimeValue> map) {
        return new DataStore(map);
    }

    public void put(String key, RuntimeValue value) {
        this.objects.put(key, value);
    }

    public RuntimeValue get(String key) {
        return this.objects.getOrDefault(key, null);
    }

    public Object2ObjectAVLTreeMap<String, RuntimeValue> map() {
        return this.objects;
    }
}
