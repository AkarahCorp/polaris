package dev.akarah.cdata.script.type;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

public interface Type<T> {
    String typeName();
    Class<T> typeClass();
    ClassDesc classDescType();

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

    static ListType list(Type<?> subtype) {
        return new ListType(subtype);
    }

    static DictionaryType dict(Type<?> keyType, Type<?> valueType) {
        return new DictionaryType(keyType, valueType);
    }

    static EntityType entity() {
        return new EntityType();
    }

    static ItemType itemStack() {
        return new ItemType();
    }

    default boolean typeEquals(Type<?> other) {
        return this.typeName().equals((other.typeName()))
                || this.typeName().equals("any")
                || other.typeName().equals("any");
    }

    default Type<?> or(Type<?> right) {
        if(!this.typeName().equals("any")) {
            return this;
        }
        if(!right.typeName().equals("any")) {
            return right;
        }
        return this;
    }
}
