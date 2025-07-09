package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.ClassDesc;

public record VectorType() implements Type<Vec3> {
    @Override
    public String typeName() {
        return "vector";
    }

    @Override
    public Class<Vec3> typeClass() {
        return Vec3.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(Vec3.class);
    }
}
