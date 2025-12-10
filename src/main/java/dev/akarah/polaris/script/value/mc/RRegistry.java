package dev.akarah.polaris.script.value.mc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.value.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RRegistry extends RuntimeValue {
    private final Registry<RuntimeValue> inner;

    public RRegistry(Registry<RuntimeValue> inner) {
        this.inner = inner;
    }

    public static RRegistry of(Registry<RuntimeValue> values) {
        return new RRegistry(values);
    }

    public static String typeName() {
        return "registry";
    }

    @MethodTypeHint(signature = "<T>(this: registry[T], entry: identifier) -> nullable[T]", documentation = "Gets a value from the list.")
    public static RNullable get(RRegistry $this, RIdentifier identifier) {
        try {
            return RNullable.of($this.inner.getValue(identifier.javaValue()));
        } catch (Exception e) {
            return RNullable.empty();
        }
    }

    @MethodTypeHint(signature = "<T>(this: list[T], value: T) -> boolean", documentation = "Returns true if the registry contains the provided key.")
    public static RBoolean contains(RRegistry $this, RIdentifier value) {
        return RBoolean.of($this.inner.containsKey(value.javaValue()));
    }

    @MethodTypeHint(signature = "<T>(this: registry[T]) -> list[identifier]", documentation = "Returns a list of all valid IDs in the registry.")
    public static RList keys(RRegistry $this) {
        return RList.of($this.inner.keySet().stream().map(RIdentifier::of).map(x -> (RuntimeValue) x).toList());
    }

    @MethodTypeHint(signature = "<T>(this: registry[T]) -> list[T]", documentation = "Returns a list of all values in the registry.")
    public static RList values(RRegistry $this) {
        return RList.of($this.inner.keySet().stream().map(x -> $this.inner.get(x).orElseThrow().value()).toList());
    }

    @MethodTypeHint(signature = "<T>(this: registry[T]) -> number", documentation = "Returns the number of entries.")
    public static RNumber size(RRegistry $this) {
        return RNumber.of($this.inner.size());
    }

    @Override
    public Registry<RuntimeValue> javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return RRegistry.of(this.inner);
    }
}
