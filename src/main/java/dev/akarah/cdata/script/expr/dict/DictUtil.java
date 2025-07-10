package dev.akarah.cdata.script.expr.dict;

import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class DictUtil {
    public static void bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("dict"), CreateDictExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("dict/put"), DictPutExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("dict/get"), DictGetExpression.class);
    }
}
