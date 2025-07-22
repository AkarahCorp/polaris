package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

public class RNumber extends RuntimeValue {
    private final double inner;

    private RNumber(double inner) {
        this.inner = inner;
    }

    public static RNumber of(double value) {
        return new RNumber(value);
    }

    @Override
    public Double javaValue() {
        return this.inner;
    }

    public double doubleValue() {
        return this.inner;
    }

    public int intValue() { return (int) this.inner; }

    @Override
    public String toString() {
        return Double.toString(this.inner);
    }

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the lowest nearest integer.")
    public static RNumber floor(RNumber number) {
        return RNumber.of(Math.floor(number.doubleValue()));
    }

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the highest nearest integer.")
    public static RNumber ceil(RNumber number) {
        return RNumber.of(Math.ceil(number.doubleValue()));
    }

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the nearest integer.")
    public static RNumber round(RNumber number) {
        return RNumber.of(Math.round(number.doubleValue()));
    }

    @MethodTypeHint(signature = "(n: number, factor: number) -> number", documentation = "Returns the nearest multiple of the provided factor.")
    public static RNumber round_to(RNumber number, RNumber factor) {
        var doubleFactor = factor.doubleValue();
        return RNumber.of(Math.round(number.doubleValue() / doubleFactor) * doubleFactor);
    }
}
