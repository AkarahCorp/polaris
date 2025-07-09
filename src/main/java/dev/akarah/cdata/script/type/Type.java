package dev.akarah.cdata.script.type;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.params.ExpressionTypeSet;

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
                if(subtype == null) {
                    sb.append("null");
                } else {
                    sb.append(subtype.verboseTypeName());
                }
                idx += 1;
                if(idx != this.subtypes().size()) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * Resolves type variables, using this type as a basis, and the incoming type to match type variables against.
     * This = Has no type variables.
     * Incoming Match = Does have type variables.
     * @param incomingMatch The incoming type variable to match variables for.
     * @param typeSet The type set to write variable modifications to.
     * @return The new variant of `incomingMatch`, with type variables sufficiently replaced.
     */
    default Type<?> resolveTypeVariables(Type<?> incomingMatch, ExpressionTypeSet typeSet) {
        System.out.println("===> Matching type " + this.verboseTypeName() + " against pattern " + incomingMatch.verboseTypeName());
        if(this instanceof SpannedType<?, ?> spannedType) {
            return spannedType.type().resolveTypeVariables(incomingMatch, typeSet);
        }
        switch (incomingMatch) {
            case VariableType matchingVarType -> {
                System.out.println("Resolving variable type `" + matchingVarType.variableName() + "` for type `" + this.verboseTypeName() + "`");
                typeSet.resolveTypeVariable(matchingVarType.variableName(), this);
            }
            case ListType matchListType -> {
                if(this instanceof ListType(Type<?> subtype)) {
                    subtype.resolveTypeVariables(matchListType.subtype(), typeSet);
                } else {
                    System.out.println("==== /!\\ Match provided is a list, but the expression is not (" + this.verboseTypeName() + ")");
                }
            }
            case DictionaryType matchDictType -> {
                if(this instanceof DictionaryType(Type<?> keyType, Type<?> valueType)) {
                    keyType.resolveTypeVariables(matchDictType.keyType(), typeSet);
                    valueType.resolveTypeVariables(matchDictType.valueType(), typeSet);
                } else {
                    System.out.println("==== /!\\ Match provided is a dict, but the expression is not (" + this.verboseTypeName() + ")");
                }
            }
            default -> {}
        }
        System.out.println("<===");
        return incomingMatch.fixTypeVariables(typeSet);
    }

    /**
     * Returns a new time, that applied a patch to the current type instance. The patch replaces all `VariableType`
     * with their associated correct types.
     * @param typeSet The type set to use for resolving type variables.
     * @return A new instance of this type.
     */
    default Type<?> fixTypeVariables(ExpressionTypeSet typeSet) {
        return switch (this) {
            case VariableType variableType -> typeSet.resolveTypeVariable(variableType.variableName());
            case ListType listType -> Type.list(
                    listType.subtype().fixTypeVariables(typeSet)
            );
            case DictionaryType dictionaryType -> Type.dict(
                    dictionaryType.keyType().fixTypeVariables(typeSet),
                    dictionaryType.valueType().fixTypeVariables(typeSet)
            );
            default -> this;
        };
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

    static IdentifierType identifier() {
        return new IdentifierType();
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

    static VariableType var(ExpressionTypeSet typeSet, String name) {
        return new VariableType(typeSet, name);
    }

    default boolean typeEquals(Type<?> other) {
        if(other == null) {
            return false;
        }
        if(other.typeName().equals("any")) {
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
