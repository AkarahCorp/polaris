package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import net.minecraft.resources.ResourceLocation;

import java.lang.constant.ClassDesc;

public record IdentifierType() implements Type<ResourceLocation> {
    @Override
    public String typeName() {
        return "identifier";
    }

    @Override
    public Class<ResourceLocation> typeClass() {
        return ResourceLocation.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(ResourceLocation.class);
    }
}
