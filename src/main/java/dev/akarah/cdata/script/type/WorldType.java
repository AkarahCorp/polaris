
package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import net.minecraft.world.level.Level;

import java.lang.constant.ClassDesc;

public record WorldType() implements Type<Level> {
    @Override
    public String typeName() {
        return "world";
    }

    @Override
    public Class<Level> typeClass() {
        return Level.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(Level.class);
    }
}
