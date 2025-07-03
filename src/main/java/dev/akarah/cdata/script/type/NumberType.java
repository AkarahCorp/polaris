package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.env.JIT;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

public record NumberType() implements Type<Double> {
    @Override
    public String typeName() {
        return "number";
    }

    @Override
    public Class<Double> typeClass() {
        return Double.class;
    }

    @Override
    public ClassDesc classDescType() {
        return JIT.ofClass(Double.class);
    }
}
