package dev.akarah.cdata.script.type;

import java.lang.classfile.TypeKind;

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
    public TypeKind classFileType() {
        return TypeKind.VOID;
    }
}
