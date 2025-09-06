package dev.akarah.polaris.script.value;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class RNumber extends RuntimeValue implements Comparable<RNumber> {
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

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the sine of this number.")
    public static RNumber sin(RNumber number) {
        return RNumber.of(Math.sin(number.doubleValue()));
    }

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the cosine of this number.")
    public static RNumber cos(RNumber number) {
        return RNumber.of(Math.cos(number.doubleValue()));
    }

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the tangent of this number.")
    public static RNumber tan(RNumber number) {
        return RNumber.of(Math.sin(number.doubleValue()));
    }

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the lowest nearest integer.")
    public static RNumber floor(RNumber number) {
        return RNumber.of(Math.floor(number.doubleValue()));
    }

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the highest nearest integer.")
    public static RNumber ceil(RNumber number) {
        return RNumber.of(Math.ceil(number.doubleValue()));
    }

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the square root of this number.")
    public static RNumber sqrt(RNumber number) {
        return RNumber.of(Math.sqrt(number.doubleValue()));
    }

    @MethodTypeHint(signature = "(n: number) -> number", documentation = "Returns the cube root of this number.")
    public static RNumber cbrt(RNumber number) {
        return RNumber.of(Math.cbrt(number.doubleValue()));
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

    @MethodTypeHint(signature = "(lhs: number, rhs: number) -> number", documentation = "Returns the sum of two numbers.")
    public static RNumber add(RNumber lhs, RNumber rhs) {
        return RNumber.of(lhs.doubleValue() + rhs.doubleValue());
    }

    @MethodTypeHint(signature = "(lhs: number, rhs: number) -> number", documentation = "Returns the difference of two numbers.")
    public static RNumber sub(RNumber lhs, RNumber rhs) {
        return RNumber.of(lhs.doubleValue() - rhs.doubleValue());
    }

    @MethodTypeHint(signature = "(lhs: number, rhs: number) -> number", documentation = "Returns the product of two numbers.")
    public static RNumber mul(RNumber lhs, RNumber rhs) {
        return RNumber.of(lhs.doubleValue() * rhs.doubleValue());
    }

    @MethodTypeHint(signature = "(lhs: number, rhs: number) -> number", documentation = "Returns the quotient of two numbers.")
    public static RNumber div(RNumber lhs, RNumber rhs) {
        return RNumber.of(lhs.doubleValue() / rhs.doubleValue());
    }

    @MethodTypeHint(signature = "(lhs: number, rhs: number) -> number", documentation = "Returns the remainder of two numbers.")
    public static RNumber rem(RNumber lhs, RNumber rhs) {
        return RNumber.of(lhs.doubleValue() % rhs.doubleValue());
    }

    @MethodTypeHint(signature = "(lhs: number, rhs: number) -> boolean", documentation = "Compares two numbers with the specified operation.")
    public static RBoolean greater_than(RNumber lhs, RNumber rhs) {
        return RBoolean.of(lhs.doubleValue() > rhs.doubleValue());
    }
    @MethodTypeHint(signature = "(lhs: number, rhs: number) -> boolean", documentation = "Compares two numbers with the specified operation.")
    public static RBoolean greater_than_or_equal_to(RNumber lhs, RNumber rhs) {
        return RBoolean.of(lhs.doubleValue() >= rhs.doubleValue());
    }

    @MethodTypeHint(signature = "(lhs: number, rhs: number) -> boolean", documentation = "Compares two numbers with the specified operation.")
    public static RBoolean less_than(RNumber lhs, RNumber rhs) {
        return RBoolean.of(lhs.doubleValue() < rhs.doubleValue());
    }
    @MethodTypeHint(signature = "(lhs: number, rhs: number) -> boolean", documentation = "Compares two numbers with the specified operation.")
    public static RBoolean less_than_or_equal_to(RNumber lhs, RNumber rhs) {
        return RBoolean.of(lhs.doubleValue() <= rhs.doubleValue());
    }

    @Override
    public String toString() {
        var df = new DecimalFormat("###,###,###,###,###,###,###,###,###,###.###");
        df.setRoundingMode(RoundingMode.HALF_UP);

        var value =  df.format(this.inner);
        if(value.endsWith(".0")) {
            value = value.replace(".0", "");
        }
        return value;
    }

    @Override
    public int compareTo(@NotNull RNumber rNumber) {
        return Double.compare(this.inner, rNumber.inner);
    }
}
