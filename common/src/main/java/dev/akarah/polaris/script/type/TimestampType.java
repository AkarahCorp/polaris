package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RTimestamp;

import java.lang.constant.ClassDesc;

public record TimestampType() implements Type<RTimestamp> {
    @Override
    public String typeName() {
        return "timestamp";
    }

    @Override
    public Class<RTimestamp> typeClass() {
        return RTimestamp.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RTimestamp.class);
    }
}
