package dev.akarah.polaris.script.value;

import java.lang.invoke.MethodHandle;

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
