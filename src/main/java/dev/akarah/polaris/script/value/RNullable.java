package dev.akarah.polaris.script.value;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;

import java.util.Optional;

public class RNullable extends RuntimeValue {
    private RuntimeValue inner;

    private RNullable(RuntimeValue inner) {
        this.inner = inner;
    }

    @MethodTypeHint(signature = "<T>() -> nullable[T]", documentation = "Creates an empty nullable instance.")
    public static RNullable empty() {
        return new RNullable(null);
    }

    @MethodTypeHint(signature = "<T>(value: T) -> nullable[T]", documentation = "Creates a nullable instance with a value inside.")
    public static RNullable of(RuntimeValue value) {
        return new RNullable(value);
    }

    @MethodTypeHint(signature = "<T>(wrapper: nullable[T], value: T) -> void", documentation = "Sets the value inside of the nullable instance.")
    public static void set(RNullable nullable, RuntimeValue value) {
        nullable.inner = value;
    }

    @MethodTypeHint(signature = "(value: nullable[any]) -> boolean", documentation = "Returns true if the value inside is null.")
    public static RBoolean is_null(RNullable $this) {
        return RBoolean.of($this.inner == null);
    }

    @MethodTypeHint(signature = "(value: nullable[any]) -> boolean", documentation = "Returns true if the value inside is not null.")
    public static RBoolean is_nonnull(RNullable $this) {
        return RBoolean.of($this.inner != null);
    }

    @MethodTypeHint(signature = "<T>(value: nullable[T]) -> T", documentation = "Unwraps the value inside, throwing an exception if it is null.")
    public static RuntimeValue unwrap(RNullable $this) {
        if($this.inner == null) {
            throw new RuntimeException("Can not unwrap null value.");
        }
        return $this.inner;
    }

    @MethodTypeHint(signature = "<T>(value: nullable[T], callback: function(T) -> void) -> void", documentation = "Runs the callback if the value inside is not null.")
    public static void if_present(RNullable $this, RFunction function) {
        if($this.inner != null) {
            try {
                function.javaValue().invoke($this.inner);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @MethodTypeHint(
            signature = "<T>(value: nullable[T], callback: function(T) -> T) -> nullable[T]",
            documentation = "If the value inside is present, it will return a new nullable instance with the value returned from the callback. " +
                    "Otherwise, it will return an empty nullable instance."
    )
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

    @MethodTypeHint(
            signature = "<T>(value: nullable[T], alternative: T) -> T",
            documentation = "If this value is present, returns the value inside of this nullable. Otherwise, it will return the alternative provided."
    )
    public static RuntimeValue or_else(RNullable $this, RuntimeValue fallback) {
        return $this.inner == null ? fallback : $this.inner;
    }

    @Override
    public Optional<RuntimeValue> javaValue() {
        return Optional.ofNullable(this.inner);
    }

    @Override
    public RuntimeValue copy() {
        return RNullable.of(this.inner);
    }
}
