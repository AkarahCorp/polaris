package dev.akarah.cdata.script.value;

import java.util.HashMap;
import java.util.Map;

public class RDict {
    Map<Object, Object> internal = new HashMap<>();

    public static RDict create() {
        return new RDict();
    }

    public Object get(Object key) {
        return this.internal.get(key);
    }

    public void put(Object key, Object value) {
        this.internal.put(key, value);
    }
}
