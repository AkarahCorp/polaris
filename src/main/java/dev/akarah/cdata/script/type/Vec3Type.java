package dev.akarah.cdata.script.type;

import net.minecraft.world.phys.Vec3;

public record Vec3Type() implements Type<Vec3> {
    @Override
    public String typeName() {
        return "vec3";
    }

    @Override
    public Class<Vec3> typeClass() {
        return Vec3.class;
    }
}
