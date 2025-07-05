package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;

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
        return CodegenUtil.ofClass(Double.class);
    }
}
