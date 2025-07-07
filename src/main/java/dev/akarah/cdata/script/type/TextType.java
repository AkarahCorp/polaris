package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import net.minecraft.network.chat.Component;

import java.lang.constant.ClassDesc;

public record TextType() implements Type<Component> {
    @Override
    public String typeName() {
        return "text";
    }

    @Override
    public Class<Component> typeClass() {
        return Component.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(Component.class);
    }
}
