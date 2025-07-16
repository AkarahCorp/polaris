package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RNullable extends RuntimeValue<Optional<RuntimeValue<?>>> {
    private final RuntimeValue<?> inner;

    private RNullable(RuntimeValue<?> inner) {
        this.inner = inner;
    }

    @MethodTypeHint("<T>() -> nullable[T]")
    public static RNullable empty() {
        return new RNullable(null);
    }

    @MethodTypeHint("<T>(value: T) -> nullable[T]")
    public static RNullable of(RuntimeValue<?> value) {
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
    public static RuntimeValue<?> unwrap(RNullable $this) {
        if($this.inner == null) {
            throw new RuntimeException("Can not unwrap null value.");
        }
        return $this.inner;
    }

    @Override
    public Optional<RuntimeValue<?>> javaValue() {
        return Optional.ofNullable(this.inner);
    }
}
