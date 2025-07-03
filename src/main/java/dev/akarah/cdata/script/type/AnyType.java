package dev.akarah.cdata.script.type;


import dev.akarah.cdata.script.env.JIT;

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
        return JIT.ofClass(Object.class);
    }
}
