package dev.akarah.cdata.script.type;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.util.List;

public interface Type<T> {
    String typeName();
    Class<T> typeClass();
    ClassDesc classDescType();

    default TypeKind classFileType() {
        return TypeKind.REFERENCE;
    }
    default List<Type<?>> subtypes() { return List.of(); }

    default String verboseTypeName() {
        var sb = new StringBuilder();
        sb.append(this.typeName());
        if(!this.subtypes().isEmpty()) {
            sb.append("[");
            int idx = 0;
            for(var subtype : this.subtypes()) {
                sb.append(subtype.verboseTypeName());
                idx += 1;
                if(idx != this.subtypes().size()) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        return sb.toString();
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
        if(this.typeName().equals("any") || other.typeName().equals("any")) {
            return true;
        }

        var basicTypeCondition = this.typeName().equals(other.typeName());

        var subtypeConditions = this.subtypes().size() == other.subtypes().size()
                && Streams.zip(this.subtypes().stream(), other.subtypes().stream(), Pair::of)
                        .filter(x -> x.getFirst().typeEquals(x.getSecond()))
                        .count() == this.subtypes().size();

        return basicTypeCondition && subtypeConditions;
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
