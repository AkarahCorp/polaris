package dev.akarah.cdata.script.value;

import com.google.common.collect.Maps;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

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

    @MethodTypeHint(signature = "<K, V>(dictionary: dict[K, V], key: K, value: V) -> V", documentation = "Inserts a value into the dictionary.")
    public static void put(RDict dict, RuntimeValue key, RuntimeValue value) {
        dict.inner.put(key, value);
    }

    @Override
    public Map<RuntimeValue, RuntimeValue> javaValue() {
        return this.inner;
    }
}
