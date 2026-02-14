package dev.akarah.polaris.script.jvm;

import java.lang.constant.ClassDesc;

public class CodegenUtil {
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
