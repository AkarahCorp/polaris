package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.env.JIT;
import net.minecraft.world.entity.Entity;

import java.lang.constant.ClassDesc;
import java.util.Map;

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
        return JIT.ofClass(Entity.class);
    }
}
