package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

public class RFunction extends RuntimeValue {
    private final MethodHandle inner;

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
}
