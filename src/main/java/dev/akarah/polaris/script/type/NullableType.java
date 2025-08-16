package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RNullable;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

public record NullableType(Type<?> subtype) implements Type<RNullable> {
    @Override
    public String typeName() {
        return "nullable";
    }

    @Override
    public Class<RNullable> typeClass() {
        return RNullable.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RNullable.class);
    }

    @Override
    public List<Type<?>> subtypes() {
        var a = new ArrayList<Type<?>>();
        a.add(subtype);
        return a;
    }
}
