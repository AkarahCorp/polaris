package dev.akarah.cdata.script.type;


import dev.akarah.cdata.script.jvm.CodegenUtil;

import java.lang.constant.ClassDesc;

public record AnyType() implements Type<Object> {
    @Override
    public String typeName() {
        return "any";
    }

    @Override
    public Class<Object> typeClass() {
        return Object.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(Object.class);
    }
}
