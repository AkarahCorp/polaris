package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.env.JIT;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Map;

public record DictionaryType() implements Type<Map<Object, Object>> {
    @Override
    public String typeName() {
        return "dict";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<Object, Object>> typeClass() {
        return (Class<Map<Object, Object>>) (Class<?>) Map.class;
    }

    @Override
    public ClassDesc classDescType() {
        return JIT.ofClass(Map.class);
    }
}
