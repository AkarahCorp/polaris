package dev.akarah.polaris.script.value;

import com.google.common.collect.Maps;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;

import java.util.Map;

public class RDict extends RuntimeValue {
    private final Map<RuntimeValue, RuntimeValue> inner;

    public RDict(Map<RuntimeValue, RuntimeValue> inner) {
        this.inner = inner;
    }

    @MethodTypeHint(signature = "<K, V>() -> dict[K, V]", documentation = "Creates a new empty dictionary.")
    public static RDict create() {
        return new RDict(Maps.newHashMap());
    }

    @MethodTypeHint(signature = "<K, V>(dictionary: dict[K, V], key: K) -> nullable[V]", documentation = "Gets a value from the dictionary.")
    public static RNullable get(RDict dict, RuntimeValue key) {
        return RNullable.of(dict.inner.get(key));
    }

    @MethodTypeHint(signature = "<K, V>(dictionary: dict[K, V], key: K) -> V", documentation = "Gets a value from the dictionary, throwing if the value is not present.")
    public static RuntimeValue get_or_throw(RDict dict, RuntimeValue key) {
        return RNullable.unwrap(RNullable.of(dict.inner.get(key)));
    }

    @MethodTypeHint(signature = "<K, V>(dictionary: dict[K, V], key: K, value: V) -> void", documentation = "Inserts a value into the dictionary.")
    public static void put(RDict dict, RuntimeValue key, RuntimeValue value) {
        dict.inner.put(key, value);
    }

    @MethodTypeHint(signature = "<K, V>(dictionary: dict[K, V], key: K) -> void", documentation = "Removes a key from the dictionary.")
    public static void remove(RDict dict, RuntimeValue key) {
        dict.inner.remove(key);
    }

    @MethodTypeHint(signature = "<K, V>(dictionary: dict[K, V]) -> list[K]", documentation = "Lists all keys from the dictionary.")
    public static RList keys(RDict dict) {
        var list = RList.create();
        for(var key : dict.javaValue().keySet()) {
            RList.add(list, key);
        }
        return list;
    }

    @MethodTypeHint(signature = "<K, V>(dictionary: dict[K, V]) -> number", documentation = "Returns the amount of entries in the dictionary.")
    public static RNumber size(RDict dict) {
        return RNumber.of(dict.inner.size());
    }

    @Override
    public Map<RuntimeValue, RuntimeValue> javaValue() {
        return this.inner;
    }
}
