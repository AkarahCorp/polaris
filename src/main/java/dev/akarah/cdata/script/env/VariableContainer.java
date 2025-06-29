package dev.akarah.cdata.script.env;

import java.util.HashMap;
import java.util.Map;

public class VariableContainer {
    Map<String, Object> inner;

    private VariableContainer(Map<String, Object> map) {
        this.inner = map;
    }

    public Object lookup(String name) {
        return inner.getOrDefault(name, null);
    }

    public void put(String name, Object value) {
        this.inner.put(name, value);
    }

    public static VariableContainer empty() {
        return new VariableContainer(new HashMap<>());
    }
}
