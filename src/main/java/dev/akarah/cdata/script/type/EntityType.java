package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.REntity;
import net.minecraft.world.entity.Entity;

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
