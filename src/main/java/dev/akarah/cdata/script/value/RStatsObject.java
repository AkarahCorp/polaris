package dev.akarah.cdata.script.value;

import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import net.minecraft.network.chat.Component;

public class RStatsObject extends RuntimeValue {
    private StatsObject inner;

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

    @MethodTypeHint(signature = "(object: stat_obj, key: string, value: number) -> void", documentation = "?")
    public static void add(RStatsObject object, RString key, RNumber value) {
        object.inner = object.inner.copy();
        var finalValue = object.javaValue().get(key.javaValue()) + value.doubleValue();
        object.javaValue().set(key.javaValue(), finalValue);
    }

    @MethodTypeHint(signature = "(object: stat_obj, value: number) -> void", documentation = "?")
    public static void multiply(RStatsObject object, RNumber number) {
        object.inner = object.inner.copy();
        for(var entry : object.javaValue().keySet()) {
            object.javaValue().set(entry, object.javaValue().get(entry) * number.doubleValue());
        }
    }
}
