package dev.akarah.polaris.script.value;

import com.google.common.collect.Lists;
import dev.akarah.polaris.registry.stat.StatsObject;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.value.mc.RIdentifier;

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

    @MethodTypeHint(signature = "(object: stat_obj, key: identifier) -> number", documentation = "Get the value of a stat in this stat object.")
    public static RNumber get(RStatsObject object, RIdentifier key) {
        return RNumber.of(object.javaValue().get(key.javaValue()));
    }

    @MethodTypeHint(signature = "(object: stat_obj, name: text, key: identifier, value: number) -> void", documentation = "Add a value to the stat in this stat object.")
    public static void add(RStatsObject object, RText name, RIdentifier key, RNumber value) {
        object.javaValue().add(new StatsObject.SourceEntry(
                name.javaValue(),
                key.javaValue(),
                StatsObject.SourceOperation.ADD,
                value.doubleValue()
        ));
    }

    @MethodTypeHint(signature = "(object: stat_obj, name: text, key: string, value: number) -> void", documentation = "Add a value to the stat in this stat object.")
    public static void add_multiplier(RStatsObject object, RText name, RIdentifier key, RNumber value) {
        object.javaValue().add(new StatsObject.SourceEntry(
                name.javaValue(),
                key.javaValue(),
                StatsObject.SourceOperation.MULTIPLY,
                value.doubleValue()
        ));
    }

    @MethodTypeHint(signature = "(object: stat_obj, name: text, key: string, value: number) -> void", documentation = "Add a value to the stat in this stat object.")
    public static void add_percentage(RStatsObject object, RText name, RIdentifier key, RNumber value) {
        object.javaValue().add(new StatsObject.SourceEntry(
                name.javaValue(),
                key.javaValue(),
                StatsObject.SourceOperation.PERCENTAGE,
                value.doubleValue()
        ));
    }

    @MethodTypeHint(signature = "(object: stat_obj, value: number) -> void", documentation = "Multiplies all addition modifiers in the stat object.")
    public static void multiply(RStatsObject object, RNumber value) {
        var sources = Lists.<StatsObject.SourceEntry>newArrayList();

        for(var source : object.javaValue().sources) {
            sources.add(new StatsObject.SourceEntry(
                    source.title(),
                    source.stat(),
                    source.operation(),
                    source.value() * value.doubleValue()
            ));
        }

        object.inner.sources = sources;
    }

    @MethodTypeHint(signature = "(object: stat_obj, other: stat_obj, name: text) -> void", documentation = "Adds all values in the other stat object to this tat object.")
    public static void add_all_with_name(RStatsObject object, RStatsObject other, RText name) {
        object.javaValue().addAllUnderName(other.javaValue(), name.javaValue());
    }

    @MethodTypeHint(signature = "(object: stat_obj, other: stat_obj) -> void", documentation = "Adds all values in the other stat object to this tat object.")
    public static void add_all_direct(RStatsObject object, RStatsObject other) {
        object.javaValue().add(other.javaValue());
    }

    @MethodTypeHint(signature = "(object: stat_obj) -> stat_obj", documentation = "Creates a copy of this stat object.")
    public static RStatsObject copy(RStatsObject object) {
        return RStatsObject.of(object.javaValue().copy());
    }
}
