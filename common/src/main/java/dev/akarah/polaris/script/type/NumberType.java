package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RNumber;

import java.lang.constant.ClassDesc;

public record NumberType() implements Type<RNumber> {
    @Override
    public String typeName() {
        return "number";
    }

    @Override
    public Class<RNumber> typeClass() {
        return RNumber.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RNumber.class);
    }
}
