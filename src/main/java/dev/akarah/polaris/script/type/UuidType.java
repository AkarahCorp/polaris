package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RUuid;

import java.lang.constant.ClassDesc;

public record UuidType() implements Type<RUuid> {
    @Override
    public String typeName() {
        return "uuid";
    }

    @Override
    public Class<RUuid> typeClass() {
        return RUuid.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RUuid.class);
    }
}
