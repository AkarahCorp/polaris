package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.mc.RRegistry;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

public record RegistryType(Type<?> subtype) implements Type<RRegistry> {
    @Override
    public String typeName() {
        return "registry";
    }

    @Override
    public Class<RRegistry> typeClass() {
        return RRegistry.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RRegistry.class);
    }

    @Override
    public List<Type<?>> subtypes() {
        var a = new ArrayList<Type<?>>();
        a.add(subtype);
        return a;
    }
}
