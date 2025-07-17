package dev.akarah.cdata.script.value;

import dev.akarah.cdata.db.DataStore;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

public class RStore extends RuntimeValue {
    private final DataStore inner;

    private RStore(DataStore inner) {
        this.inner = inner;
    }

    public static RStore of(DataStore value) {
        return new RStore(value);
    }

    @Override
    public DataStore javaValue() {
        return this.inner;
    }

    @MethodTypeHint("(this: store, key: string, value: any) -> void")
    public static void set(RStore store, RString key, RuntimeValue value) {
        store.javaValue().put(key.javaValue(), value);
    }

    @MethodTypeHint("(this: store, key: string, fallback: any?) -> any")
    public static RuntimeValue get(RStore store, RString key, RuntimeValue fallback) {
        return store.javaValue().get(key.javaValue(), fallback);
    }
}
