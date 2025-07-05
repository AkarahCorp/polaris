package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;

import java.lang.constant.ClassDesc;

public record StringType() implements Type<String> {
    @Override
    public String typeName() {
        return "string";
    }

    @Override
    public Class<String> typeClass() {
        return String.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(String.class);
    }
}
