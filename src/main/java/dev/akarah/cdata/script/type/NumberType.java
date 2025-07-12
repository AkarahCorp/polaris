package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RNumber;

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
