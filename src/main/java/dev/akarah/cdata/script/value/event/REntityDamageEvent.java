package dev.akarah.cdata.script.value.event;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.RNumber;
import dev.akarah.cdata.script.value.mc.REntity;

public class REntityDamageEvent extends REvent {
    private final REntity primary;
    private final RNumber secondary;
    public boolean cancelled;

    private REntityDamageEvent(REntity primary, RNumber secondary) {
        this.primary = primary;
        this.secondary = secondary;
        this.cancelled = false;
    }

    public static REntityDamageEvent of(REntity primary, RNumber secondary) {
        return new REntityDamageEvent(primary, secondary);
    }

    @Override
    public Void javaValue() {
        throw new RuntimeException("Not applicable to REntityEvent.");
    }

    @MethodTypeHint(signature = "(this: any) -> entity", documentation = "Returns the entity associated with this event.")
    public static REntity entity(REntityDamageEvent event) {
        return event.primary;
    }

    @MethodTypeHint(signature = "(this: any) -> number", documentation = "Returns the damage amount of this event.")
    public static RNumber damage(REntityDamageEvent event) {
        return event.secondary;
    }

    @MethodTypeHint(signature = "(this: any) -> void", documentation = "Cancels the damage dealing part of this event.")
    public static void cancel(REntityDamageEvent event) {
        event.cancelled = true;
    }
}
