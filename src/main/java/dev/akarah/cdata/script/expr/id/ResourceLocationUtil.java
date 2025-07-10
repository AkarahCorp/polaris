package dev.akarah.cdata.script.expr.id;

import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationUtil {
    public static void bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("id"), IdentifierExpression.class);
    }

    public static ResourceLocation create(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
