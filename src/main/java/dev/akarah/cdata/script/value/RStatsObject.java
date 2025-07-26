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
}
