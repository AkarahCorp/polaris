package dev.akarah.cdata.script.value;

import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import net.minecraft.network.chat.Component;

public class RStatsObject extends RuntimeValue {
    private final StatsObject inner;

    private RStatsObject(StatsObject inner) {
        this.inner = inner;
    }

    public static RStatsObject of(StatsObject value) {
        return new RStatsObject(value);
    }

    @Override
    public StatsObject javaValue() {
        return this.inner;
    }

    @MethodTypeHint(signature = "(object: stat_obj, key: string) -> number", documentation = "?")
    public static RNumber get(RStatsObject object, RString key) {
        return RNumber.of(object.javaValue().get(key.javaValue()));
    }

    @MethodTypeHint(signature = "(object: stat_obj, value: number) -> stat_obj", documentation = "?")
    public static RStatsObject multiply(RStatsObject object, RNumber number) {
        var so = StatsObject.of();
        for(var entry : object.javaValue().keySet()) {
            so.set(entry, object.javaValue().get(entry) * number.doubleValue());
        }
        return RStatsObject.of(so);
    }
}
