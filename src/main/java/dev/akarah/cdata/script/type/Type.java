package dev.akarah.cdata.script.type;

public interface Type<T> {
    String typeName();
    Class<T> typeClass();

    static NumberType number() {
        return new NumberType();
    }

    static StringType string() {
        return new StringType();
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

    default ListType<T> listOf() {
        return new ListType<>(this);
    }

    default boolean typeEquals(Type<?> other) {
        return this.typeName().equals((other.typeName()))
                || this.typeName().equals("any")
                || other.typeName().equals("any");
    }
}
