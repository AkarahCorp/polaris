package dev.akarah.cdata.script.type;


import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RuntimeValue;

import java.lang.constant.ClassDesc;

@SuppressWarnings("rawtypes")
public record AnyType() implements Type<RuntimeValue> {
    @Override
    public String typeName() {
        return "any";
    }

    @Override
    public Class<RuntimeValue> typeClass() {
        return RuntimeValue.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RuntimeValue.class);
    }
}
