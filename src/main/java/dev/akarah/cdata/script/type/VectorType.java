package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RVector;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.ClassDesc;

public record VectorType() implements Type<RVector> {
    @Override
    public String typeName() {
        return "vector";
    }

    @Override
    public Class<RVector> typeClass() {
        return RVector.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RVector.class);
    }
}
