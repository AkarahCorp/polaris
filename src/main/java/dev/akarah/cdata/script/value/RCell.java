package dev.akarah.cdata.script.value;

import com.google.common.collect.Lists;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

import java.util.List;

public class RCell extends RuntimeValue {
    private RuntimeValue inner;

    public RCell(RuntimeValue inner) {
        this.inner = inner;
    }

    @MethodTypeHint(signature = "<T>(value: T) -> cell[T]", documentation = "Creates a new cell.")
    public static RCell create(RuntimeValue value) {
        return new RCell(value);
    }

    @MethodTypeHint(signature = "<T>(this: cell[T]) -> T", documentation = "Gets a value inside of the cell.")
    public static RuntimeValue get(RCell $this) {
        return $this.inner;
    }

    @MethodTypeHint(signature = "<T>(this: cell[T], value: T) -> void", documentation = "Sets the value inside of the cell.")
    public static void set(RCell $this, RuntimeValue value) {
        $this.inner = value;
    }

    public RuntimeValue inner() {
        return this.inner;
    }

    @Override
    public Object javaValue() {
        return this.inner.javaValue();
    }
}
