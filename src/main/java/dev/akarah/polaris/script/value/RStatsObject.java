package dev.akarah.polaris.script.value;

import dev.akarah.polaris.registry.stat.StatsObject;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;

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

    @MethodTypeHint(signature = "(object: stat_obj, key: string) -> number", documentation = "Get the value of a stat in this stat object.")
    public static RNumber get(RStatsObject object, RString key) {
        return RNumber.of(object.javaValue().get(key.javaValue()));
    }

    @MethodTypeHint(signature = "(object: stat_obj, key: string, value: number) -> void", documentation = "Sets the stat in this stat object to the value provided.")
    public static void set(RStatsObject object, RString key, RNumber value) {
        object.javaValue().set(key.javaValue(), value.doubleValue());
    }

    @MethodTypeHint(signature = "(object: stat_obj, key: string, value: number) -> void", documentation = "Add a value to the stat in this stat object.")
    public static void add(RStatsObject object, RString key, RNumber value) {
        var finalValue = object.javaValue().get(key.javaValue()) + value.doubleValue();
        object.javaValue().set(key.javaValue(), finalValue);
    }

    @MethodTypeHint(signature = "(object: stat_obj, other: stat_obj) -> void", documentation = "Adds all values in the other stat object to this tat object.")
    public static void add_all(RStatsObject object, RStatsObject other) {
        object.javaValue().add(other.javaValue());
    }

    @MethodTypeHint(signature = "(object: stat_obj, value: number) -> void", documentation = "Multiply all stats in this stat object by the value provided.")
    public static void multiply(RStatsObject object, RNumber number) {
        for(var entry : object.javaValue().keySet()) {
            object.javaValue().set(entry, object.javaValue().get(entry) * number.doubleValue());
        }
    }

    @MethodTypeHint(signature = "(object: stat_obj) -> stat_obj", documentation = "Creates a copy of this stat object.")
    public static RStatsObject copy(RStatsObject object) {
        return RStatsObject.of(object.javaValue().copy());
    }
}
