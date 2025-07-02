package dev.akarah.cdata.script.type;

import java.lang.classfile.TypeKind;

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
    public TypeKind classFileType() {
        return TypeKind.DOUBLE;
    }
}
