package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RBoolean;

import java.lang.constant.ClassDesc;

public record BooleanType() implements Type<RBoolean> {
    @Override
    public String typeName() {
        return "boolean";
    }

    @Override
    public Class<RBoolean> typeClass() {
        return RBoolean.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RBoolean.class);
    }
}
