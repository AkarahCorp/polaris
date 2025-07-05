package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import net.minecraft.world.entity.Entity;

import java.lang.constant.ClassDesc;

public record EntityType() implements Type<Entity> {
    @Override
    public String typeName() {
        return "entity";
    }

    @Override
    public Class<Entity> typeClass() {
        return Entity.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(Entity.class);
    }
}
