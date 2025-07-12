package dev.akarah.cdata.script.expr.list;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.value.RList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class ListUtil {
    public static void bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("range"), NumberRangeExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("list/add"), AddListExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("list/add_all"), AddAllListExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("list/get"), GetListExpression.class);
    }


}
