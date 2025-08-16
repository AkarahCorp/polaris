package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.mc.REntity;

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
