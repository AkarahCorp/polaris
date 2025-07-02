package dev.akarah.cdata.script.type;

import java.lang.classfile.TypeKind;

public interface Type<T> {
    String typeName();
    Class<T> typeClass();

    default TypeKind classFileType() {
        return TypeKind.REFERENCE;
    }

    static NumberType number() {
        return new NumberType();
    }

    static StringType string() {
        return new StringType();
    }

    static BooleanType bool() {
        return new BooleanType();
    }

    static TextType text() {
        return new TextType();
    }

    static VoidType void_() {
        return new VoidType();
    }

    static Vec3Type vec3() {
        return new Vec3Type();
    }

    static AnyType any() {
        return new AnyType();
    }

    static ListType list() {
        return new ListType();
    }

    default boolean typeEquals(Type<?> other) {
        return this.typeName().equals((other.typeName()))
                || this.typeName().equals("any")
                || other.typeName().equals("any");
    }
}
