package dev.akarah.polaris.script.value.mc;

import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.resources.ResourceLocation;

public class RIdentifier extends RuntimeValue {
    private final ResourceLocation inner;

    private RIdentifier(ResourceLocation inner) {
        this.inner = inner;
    }

    public static RIdentifier of(ResourceLocation value) {
        return new RIdentifier(value);
    }

    public static RIdentifier of(String namespace, String path) {
        return new RIdentifier(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    @Override
    public ResourceLocation javaValue() {
        return this.inner;
    }
}
