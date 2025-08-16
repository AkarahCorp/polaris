package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.mc.RItem;

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
