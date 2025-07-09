package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;

import java.lang.constant.ClassDesc;
import java.util.Map;

public record DictionaryType(
        Type<?> keyType,
        Type<?> valueType
) implements Type<Map<Object, Object>> {
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
        return CodegenUtil.ofClass(Map.class);
    }
}
