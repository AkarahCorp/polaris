package dev.akarah.cdata.script.value.event;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.RNumber;
import dev.akarah.cdata.script.value.mc.REntity;

public class REntityDamageEvent extends REvent {
    private final REntity primary;
    private final RNumber secondary;

    private REntityDamageEvent(REntity primary, RNumber secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public static REntityDamageEvent of(REntity primary, RNumber secondary) {
        return new REntityDamageEvent(primary, secondary);
    }

    @Override
    public Void javaValue() {
        throw new RuntimeException("Not applicable to REntityEvent.");
    }

    @MethodTypeHint("(this: any) -> entity")
    public static REntity entity(REntityDamageEvent event) {
        return event.primary;
    }

    @MethodTypeHint("(this: any) -> number")
    public static RNumber damage(REntityDamageEvent event) {
        return event.secondary;
    }
}
