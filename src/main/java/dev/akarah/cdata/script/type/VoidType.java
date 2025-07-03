package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.env.JIT;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

public record VoidType() implements Type<Void> {
    @Override
    public String typeName() {
        return "void";
    }

    @Override
    public Class<Void> typeClass() {
        return Void.class;
    }

    @Override
    public ClassDesc classDescType() {
        return ClassDesc.ofDescriptor("V");
    }

    @Override
    public TypeKind classFileType() {
        return TypeKind.VOID;
    }
}
