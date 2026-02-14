package dev.akarah.polaris.script.value;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;

import java.lang.invoke.MethodHandle;

public class RFunction extends RuntimeValue {
    private final MethodHandle inner;



    public static String typeName() {
        return "function";
    }

    private RFunction(MethodHandle inner) {
        this.inner = inner;
    }

    public static RFunction of(MethodHandle inner) {
        return new RFunction(inner);
    }

    @Override
    public MethodHandle javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return RFunction.of(this.inner);
    }

    @MethodTypeHint(signature = "(f: function() -> void) -> void")
    public static void run0(RFunction $this) {
        try {
            $this.inner.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @MethodTypeHint(signature = "<T1>(f: function(T1) -> void, v1: T1) -> void")
    public static void run1(RFunction $this, RuntimeValue value1) {
        try {
            $this.inner.invoke(value1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @MethodTypeHint(signature = "<T1, T2>(f: function(T1, T2) -> void, v1: T1, v2: T2) -> void")
    public static void run2(RFunction $this, RuntimeValue value1, RuntimeValue value2) {
        try {
            $this.inner.invoke(value1, value2);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @MethodTypeHint(signature = "<T1, R>(f: function(T1) -> R, v1: T1) -> R")
    public static RuntimeValue get1(RFunction $this, RuntimeValue value1) {
        try {
            return (RuntimeValue) $this.inner.invoke(value1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
