package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RString;

import java.lang.constant.ClassDesc;

public record StringType() implements Type<RString> {
    @Override
    public String typeName() {
        return "string";
    }

    @Override
    public Class<RString> typeClass() {
        return RString.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RString.class);
    }
}
