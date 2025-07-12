package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

import java.util.HashMap;
import java.util.Map;

public class RDict extends RuntimeValue<Map<RuntimeValue<?>, RuntimeValue<?>>> {
    private final Map<RuntimeValue<?>, RuntimeValue<?>> inner = new HashMap<>();

    @MethodTypeHint("<K, V>() -> dict[K, V]")
    public static RDict create() {
        return new RDict();
    }

    @MethodTypeHint("<K, V>(dictionary: dict[K, V], key: K) -> V")
    public static RuntimeValue<?> get(RDict dict, RuntimeValue<?> key) {
        return dict.inner.get(key);
    }

    @MethodTypeHint("<K, V>(dictionary: dict[K, V], key: K, value: V) -> V")
    public static void put(RDict dict, RuntimeValue<?> key, RuntimeValue<?> value) {
        dict.inner.put(key, value);
    }

    @Override
    public Map<RuntimeValue<?>, RuntimeValue<?>> javaValue() {
        return this.inner;
    }
}
