package dev.akarah.cdata.script.expr.id;

import net.minecraft.resources.ResourceLocation;

public class ResourceLocationUtil {
    public static ResourceLocation create(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
