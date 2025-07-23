package dev.akarah.cdata.script.value;

import com.google.common.collect.Maps;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.jvm.CodegenContext;

import java.util.Arrays;
import java.util.HashMap;
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

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.inner.toString();
    }
}
