package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

public class RList {
    List<Object> internal = new ArrayList<>();

    @MethodTypeHint("<T>() -> list[T]")
    public static RList create() {
        return new RList();
    }

    @MethodTypeHint("<T>(this: list[T], index: number) -> T")
    public static Object get(RList $this, Double index) {
        return $this.internal.get(index.intValue());
    }

    @MethodTypeHint("<T>(this: list[T], value: T) -> void")
    public static void add(RList $this, Object object) {
        $this.internal.add(object);
    }

    @MethodTypeHint("<T>(this: list[T], values: list[T]) -> void")
    public static void add_all(RList $this, RList list) {
        $this.internal.addAll(list.internal);
    }
}
