package dev.akarah.cdata.script.expr.list;

import dev.akarah.cdata.script.expr.Expression;
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

    public static List<Double> range(Double min, Double max) {
        var list = Lists.<Double>newArrayList();
        var minIsLower = min < max;

        double idx = minIsLower ? min : max;
        while(idx <= (minIsLower ? max : min)) {
            list.add(idx);
            idx += 1;
        }
        return list;
    }
}
