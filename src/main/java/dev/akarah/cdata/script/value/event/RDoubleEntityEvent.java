package dev.akarah.cdata.script.value.event;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.RuntimeValue;
import dev.akarah.cdata.script.value.mc.REntity;

public class RDoubleEntityEvent extends REvent {
    private final REntity primary;
    private final REntity secondary;

    private RDoubleEntityEvent(REntity primary, REntity secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public static RDoubleEntityEvent of(REntity primary, REntity secondary) {
        return new RDoubleEntityEvent(primary, secondary);
    }

    @Override
    public Void javaValue() {
        throw new RuntimeException("Not applicable to REntityEvent.");
    }

    @MethodTypeHint("(this: any) -> entity")
    public static REntity primary(RDoubleEntityEvent event) {
        return event.primary;
    }

    @MethodTypeHint("(this: any) -> entity")
    public static REntity secondary(RDoubleEntityEvent event) {
        return event.secondary;
    }

    @MethodTypeHint("(this: any) -> entity")
    public static REntity attacker(RDoubleEntityEvent event) {
        return event.primary;
    }

    @MethodTypeHint("(this: any) -> entity")
    public static REntity victim(RDoubleEntityEvent event) {
        return event.secondary;
    }
}
