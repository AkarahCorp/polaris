package dev.akarah.cdata.script.type;

import java.util.List;

public record ListType<T>(Type<T> subtype) implements Type<List<T>> {
    @Override
    public String typeName() {
        return "list[" + subtype.typeName() + "]";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<List<T>> typeClass() {
        return (Class<List<T>>) (Class<?>) List.class;
    }
}
