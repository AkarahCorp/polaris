package dev.akarah.cdata.script.type;

import java.util.List;

public record ListType() implements Type<List<Object>> {
    @Override
    public String typeName() {
        return "list";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<List<Object>> typeClass() {
        return (Class<List<Object>>) (Class<?>) List.class;
    }
}
