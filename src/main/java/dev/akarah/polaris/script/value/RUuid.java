package dev.akarah.polaris.script.value;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;

import java.util.UUID;

public class RUuid extends RuntimeValue {
    private final UUID inner;

    private RUuid(UUID inner) {
        this.inner = inner;
    }

    public static RUuid of(UUID value) {
        return new RUuid(value);
    }

    @Override
    public UUID javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return new RUuid(this.inner);
    }


    @MethodTypeHint(signature = "(this: uuid) -> number", documentation = "Returns the most significant bits of this UUID.")
    public static RNumber most_significant_bits(RUuid $this) {
        return RNumber.of((double) $this.inner.getMostSignificantBits());
    }

    @MethodTypeHint(signature = "(this: uuid) -> number", documentation = "Returns the least significant bits of this UUID.")
    public static RNumber least_significant_bits(RUuid $this) {
        return RNumber.of((double) $this.inner.getLeastSignificantBits());
    }
}
