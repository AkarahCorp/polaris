package dev.akarah.cdata.script.type;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

public record BooleanType() implements Type<Boolean> {
    @Override
    public String typeName() {
        return "boolean";
    }

    @Override
    public Class<Boolean> typeClass() {
        return Boolean.class;
    }

    @Override
    public ClassDesc classDescType() {
        return ClassDesc.ofDescriptor("Z");
    }

    @Override
    public TypeKind classFileType() {
        return TypeKind.BOOLEAN;
    }
}
