package dev.akarah.cdata.script.type;

import net.minecraft.network.chat.Component;

public record TextType() implements Type<Component> {
    @Override
    public String typeName() {
        return "text";
    }

    @Override
    public Class<Component> typeClass() {
        return Component.class;
    }
}
