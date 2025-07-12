package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.type.Type;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

public class RDict {
    Map<Object, Object> internal = new HashMap<>();

    @MethodTypeHint("<K, V>() -> dict[K, V]")
    public static RDict create() {
        return new RDict();
    }

    @MethodTypeHint("<K, V>(dictionary: dict[K, V], key: K) -> V")
    public static Object get(RDict dict, Object key) {
        return dict.internal.get(key);
    }

    @MethodTypeHint("<K, V>(dictionary: dict[K, V], key: K, value: V) -> V")
    public static void put(RDict dict, Object key, Object value) {
        dict.internal.put(key, value);
    }
}
