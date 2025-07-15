package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.mc.REntity;

import java.lang.constant.ClassDesc;

public record EntityType() implements Type<REntity> {
    @Override
    public String typeName() {
        return "entity";
    }

    @Override
    public Class<REntity> typeClass() {
        return REntity.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(REntity.class);
    }
}
