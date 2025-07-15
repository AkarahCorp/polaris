package dev.akarah.cdata.script.value.mc;

import dev.akarah.cdata.script.value.RuntimeValue;
import net.minecraft.resources.ResourceLocation;

public class RIdentifier extends RuntimeValue<ResourceLocation> {
    private final ResourceLocation inner;

    private RIdentifier(ResourceLocation inner) {
        this.inner = inner;
    }

    public static RIdentifier of(ResourceLocation value) {
        return new RIdentifier(value);
    }

    @Override
    public ResourceLocation javaValue() {
        return this.inner;
    }
}
