
package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.mc.RWorld;

import java.lang.constant.ClassDesc;

public record WorldType() implements Type<RWorld> {
    @Override
    public String typeName() {
        return "world";
    }

    @Override
    public Class<RWorld> typeClass() {
        return RWorld.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RWorld.class);
    }
}
