package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RDict;

import java.lang.constant.ClassDesc;
import java.util.List;

public record DictionaryType(
        Type<?> keyType,
        Type<?> valueType
) implements Type<RDict> {
    @Override
    public String typeName() {
        return "dict";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<RDict> typeClass() {
        return RDict.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RDict.class);
    }

    @Override
    public List<Type<?>> subtypes() {
        return List.of(keyType, valueType);
    }
}
