package dev.akarah.polaris.script.value;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;

public class RCell extends RuntimeValue {
    private RuntimeValue inner;


    public static String typeName() {
        return "cell";
    }

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

    @Override
    public RuntimeValue copy() {
        return RCell.create(this.inner.copy());
    }
}
