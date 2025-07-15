package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.mc.RIdentifier;

import java.lang.constant.ClassDesc;

public record IdentifierType() implements Type<RIdentifier> {
    @Override
    public String typeName() {
        return "identifier";
    }

    @Override
    public Class<RIdentifier> typeClass() {
        return RIdentifier.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RIdentifier.class);
    }
}
