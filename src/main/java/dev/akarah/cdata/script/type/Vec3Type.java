package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.env.JIT;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.ClassDesc;

public record Vec3Type() implements Type<Vec3> {
    @Override
    public String typeName() {
        return "vec3";
    }

    @Override
    public Class<Vec3> typeClass() {
        return Vec3.class;
    }

    @Override
    public ClassDesc classDescType() {
        return JIT.ofClass(Vec3.class);
    }
}
