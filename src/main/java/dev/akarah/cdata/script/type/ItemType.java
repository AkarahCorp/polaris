package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.mc.RItem;

import java.lang.constant.ClassDesc;

public record ItemType() implements Type<RItem> {
    @Override
    public String typeName() {
        return "item";
    }

    @Override
    public Class<RItem> typeClass() {
        return RItem.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RItem.class);
    }
}
