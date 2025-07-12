package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

import java.util.ArrayList;
import java.util.List;

public class RList extends RuntimeValue<List<RuntimeValue<?>>> {
    private final List<RuntimeValue<?>> inner = new ArrayList<>();

    @MethodTypeHint("<T>() -> list[T]")
    public static RList create() {
        return new RList();
    }

    @MethodTypeHint("<T>(this: list[T], index: number) -> T")
    public static RuntimeValue<?> get(RList $this, RNumber index) {
        return $this.inner.get(index.javaValue().intValue());
    }

    @MethodTypeHint("<T>(this: list[T], value: T) -> void")
    public static void add(RList $this, RuntimeValue<?> object) {
        $this.inner.add(object);
    }

    @MethodTypeHint("<T>(this: list[T], values: list[T]) -> void")
    public static void add_all(RList $this, RList list) {
        $this.inner.addAll(list.inner);
    }

    @Override
    public List<RuntimeValue<?>> javaValue() {
        return this.inner;
    }
}
