package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.env.JIT;

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
        return JIT.ofClass(String.class);
    }
}
