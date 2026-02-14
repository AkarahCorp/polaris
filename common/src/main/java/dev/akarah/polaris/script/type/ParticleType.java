package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.mc.RParticle;

import java.lang.constant.ClassDesc;

public record ParticleType() implements Type<RParticle> {
    @Override
    public String typeName() {
        return "particle";
    }

    @Override
    public Class<RParticle> typeClass() {
        return RParticle.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RParticle.class);
    }
}
