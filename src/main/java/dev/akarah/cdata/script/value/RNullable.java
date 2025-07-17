package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RNullable extends RuntimeValue {
    private final RuntimeValue inner;

    private RNullable(RuntimeValue inner) {
        this.inner = inner;
    }

    @MethodTypeHint("<T>() -> nullable[T]")
    public static RNullable empty() {
        return new RNullable(null);
    }

    @MethodTypeHint("<T>(value: T) -> nullable[T]")
    public static RNullable of(RuntimeValue value) {
        return new RNullable(value);
    }

    @MethodTypeHint("(value: nullable[any]) -> boolean")
    public static RBoolean is_null(RNullable $this) {
        return RBoolean.of($this.inner == null);
    }

    @MethodTypeHint("(value: nullable[any]) -> boolean")
    public static RBoolean is_nonnull(RNullable $this) {
        return RBoolean.of($this.inner != null);
    }

    @MethodTypeHint("<T>(value: nullable[T]) -> T")
    public static RuntimeValue unwrap(RNullable $this) {
        if($this.inner == null) {
            throw new RuntimeException("Can not unwrap null value.");
        }
        return $this.inner;
    }

    @MethodTypeHint("<T>(value: nullable[T], callback: function(inventory) -> void) -> void")
    public static void if_present(RNullable $this, RFunction function) {
        if($this.inner != null) {
            try {
                function.javaValue().invoke($this.inner);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @MethodTypeHint("<T>(value: nullable[T], callback: function(T) -> T) -> nullable[T]")
    public static RNullable map(RNullable $this, RFunction function) {
        if($this.inner != null) {
            try {
                return RNullable.of((RuntimeValue) function.javaValue().invoke($this.inner));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return $this;
    }

    @Override
    public Optional<RuntimeValue> javaValue() {
        return Optional.ofNullable(this.inner);
    }
}
