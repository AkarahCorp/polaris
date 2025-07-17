package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RString;
import dev.akarah.cdata.script.value.RUuid;

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
