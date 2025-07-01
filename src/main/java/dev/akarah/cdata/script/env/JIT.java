package dev.akarah.cdata.script.env;

import java.lang.constant.ClassDesc;

public class JIT {
    public static ClassDesc ofClass(Class<?> clazz) {
        return ClassDesc.ofDescriptor(clazz.descriptorString());
    }

    public static ClassDesc ofVoid() {
        return ClassDesc.ofDescriptor("V");
    }

    public static ClassDesc ofDouble() {
        return ClassDesc.ofDescriptor("D");
    }

    public static ClassDesc ofBoolean() {
        return ClassDesc.ofDescriptor("Z");
    }

    public static ClassDesc ofInt() {
        return ClassDesc.ofDescriptor("I");
    }
}
