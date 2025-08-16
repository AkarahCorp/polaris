package dev.akarah.polaris.script.value;

import dev.akarah.polaris.db.DataStore;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;

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

    @MethodTypeHint(signature = "(this: store, key: string, value: any) -> void", documentation = "Gets the value associated with the key inside of this data store.")
    public static void set(RStore store, RString key, RuntimeValue value) {
        store.javaValue().put(key.javaValue(), value);
    }

    @MethodTypeHint(signature = "(this: store, key: string) -> nullable[any]", documentation = "Sets the value associated with the key inside of this data store.")
    public static RNullable get(RStore store, RString key) {
        return RNullable.of(store.javaValue().get(key.javaValue()));
    }
}
