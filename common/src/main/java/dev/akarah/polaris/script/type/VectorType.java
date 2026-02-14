package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.mc.RVector;

import java.lang.constant.ClassDesc;

public record VectorType() implements Type<RVector> {
    @Override
    public String typeName() {
        return "vector";
    }

    @Override
    public Class<RVector> typeClass() {
        return RVector.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RVector.class);
    }
}
