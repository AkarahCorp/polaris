package dev.akarah.cdata.script.type;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.event.*;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.util.List;

public interface Type<T> {
    String typeName();
    Class<T> typeClass();
    ClassDesc classDescType();

    static List<Type<?>> allTypes() {
        return List.of(
                Type.number(),
                Type.string(),
                Type.bool(),
                Type.list(Type.var(null, "T")),
                Type.dict(Type.var(null, "K"), Type.var(null, "V")),
                Type.nullable(Type.var(null, "T")),
                Type.vector(),
                Type.world(),
                Type.entity(),
                Type.store(),
                Type.identifier(),
                Type.inventory(),
                Type.itemStack(),
                Type.text(),
                Type.uuid()
        );
    }

    default Type<?> flatten() {
        if(this instanceof SpannedType<?> spannedType) {
            return spannedType.type().flatten();
        }
        return this;
    }

    default SpannedType<T> spanned(SpanData spanData) {
        return new SpannedType<>(this, spanData);
    }

    default TypeKind classFileType() {
        return TypeKind.REFERENCE;
    }

    default List<? extends Type<?>> subtypes() { return List.of(); }

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
    default Type<?> resolveTypeVariables(Type<?> incomingMatch, ExpressionTypeSet typeSet, SpanData fallbackSpan) {
        var this2 = this.flatten();
        incomingMatch = incomingMatch.flatten();

        SpanData span = null;
        if(this instanceof SpannedType<?> spannedType) {
            span = spannedType.span();
        } else if(incomingMatch instanceof SpannedType<?> spannedType) {
            span = spannedType.span();
        } else {
            span = fallbackSpan;
        }

        switch (incomingMatch) {
            case VariableType matchingVarType -> {
                typeSet.resolveTypeVariable(matchingVarType.variableName(), this2, span);
            }
            case ListType matchListType -> {
                if(this2 instanceof ListType(Type<?> subtype)) {
                    subtype.resolveTypeVariables(matchListType.subtype(), typeSet, fallbackSpan);
                }
            }
            case DictionaryType matchDictType -> {
                if(this2 instanceof DictionaryType(Type<?> keyType, Type<?> valueType)) {
                    keyType.resolveTypeVariables(matchDictType.keyType(), typeSet, fallbackSpan);
                    valueType.resolveTypeVariables(matchDictType.valueType(), typeSet, fallbackSpan);
                }
            }
            case NullableType matchNullableType -> {
                if(this2 instanceof NullableType(Type<?> subtype)) {
                    subtype.resolveTypeVariables(matchNullableType.subtype(), typeSet, fallbackSpan);
                }
            }
            default -> {}
        }
        return incomingMatch.fixTypeVariables(typeSet);
    }

    /**
     * Returns a new time, that applied a patch to the current type instance. The patch replaces all `VariableType`
     * with their associated correct types.
     * @param typeSet The type set to use for resolving type variables.
     * @return A new instance of this type.
     */
    default Type<?> fixTypeVariables(ExpressionTypeSet typeSet) {
        return switch (this.flatten()) {
            case VariableType variableType -> typeSet.resolveTypeVariable(variableType.variableName());
            case ListType listType -> Type.list(
                    listType.subtype().fixTypeVariables(typeSet)
            );
            case DictionaryType dictionaryType -> Type.dict(
                    dictionaryType.keyType().fixTypeVariables(typeSet),
                    dictionaryType.valueType().fixTypeVariables(typeSet)
            );

            case NullableType nullableType -> Type.nullable(
                    nullableType.subtype().fixTypeVariables(typeSet)
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

    static VectorType vector() {
        return new VectorType();
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

    static WorldType world() {
        return new WorldType();
    }

    static UuidType uuid() {
        return new UuidType();
    }

    static StoreType store() {
        return new StoreType();
    }

    static NullableType nullable(Type<?> subtype) {
        return new NullableType(subtype);
    }

    static InventoryType inventory() {
        return new InventoryType();
    }

    static FunctionType function(Type<?> returnType, List<? extends Type<?>> parameters) {
        return new FunctionType(returnType, parameters);
    }

    static StructType struct(String name, List<StructType.Field> fields) {
        return new StructType(name, fields);
    }

    static VariableType var(ExpressionTypeSet typeSet, String name) {
        return new VariableType(typeSet, name);
    }

    static Events events() {
        return new Events();
    }

    class Events {
        public EntityEventType entity(String name) {
            return new EntityEventType(name);
        }

        public DoubleEntityEventType doubleEntity(String name) {
            return new DoubleEntityEventType(name);
        }

        public EntityItemEventType entityItem(String name) {
            return new EntityItemEventType(name);
        }

        public EntityDamageEventType entityDamage(String name) {
            return new EntityDamageEventType(name);
        }

        public ItemEventType item(String name) {
            return new ItemEventType(name);
        }

        public EmptyEventType empty(String name) { return new EmptyEventType(name); }
    }

    default boolean typeEquals(Type<?> other) {
        if(other == null) {
            return false;
        }
        if(this.flatten() instanceof StructType thisStruct && other.flatten() instanceof StructType otherStruct) {
            return thisStruct.name().equals(otherStruct.name());
        }
        if(this.typeName().equals("any")) {
            return true;
        }
        if(other.typeName().equals("any")) {
            return true;
        }

        var basicTypeCondition = this.typeName().equals(other.typeName()) || this.verboseTypeName().equals(other.verboseTypeName());

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
