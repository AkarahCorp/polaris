package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RStore;

import java.lang.constant.ClassDesc;

public record StoreType() implements Type<RStore> {
    @Override
    public String typeName() {
        return "store";
    }

    @Override
    public Class<RStore> typeClass() {
        return RStore.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RStore.class);
    }
}
