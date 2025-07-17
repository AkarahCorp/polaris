package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

import java.util.HashMap;
import java.util.Map;

public class RStruct extends RuntimeValue {
    private final RuntimeValue[] inner;

    private RStruct(RuntimeValue[] inner) {
        this.inner = inner;
    }

    public static RStruct create(int size) {
        return new RStruct(new RuntimeValue[size]);
    }

    public static RuntimeValue get(RStruct dict, int key) {
        return dict.inner[key];
    }

    public static void put(RStruct dict, int key, RuntimeValue value) {
        dict.inner[key] = value;
    }

    @Override
    public RuntimeValue[] javaValue() {
        return this.inner;
    }
}
