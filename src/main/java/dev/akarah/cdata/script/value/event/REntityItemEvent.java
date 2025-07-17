package dev.akarah.cdata.script.value.event;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.mc.REntity;
import dev.akarah.cdata.script.value.mc.RItem;

public class REntityItemEvent extends REvent {
    private final REntity primary;
    private final RItem secondary;

    private REntityItemEvent(REntity primary, RItem secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public static REntityItemEvent of(REntity primary, RItem secondary) {
        return new REntityItemEvent(primary, secondary);
    }

    @Override
    public Void javaValue() {
        throw new RuntimeException("Not applicable to REntityEvent.");
    }

    @MethodTypeHint(signature = "(this: any) -> entity", documentation = "Returns the entity associated with this event.")
    public static REntity entity(REntityItemEvent event) {
        return event.primary;
    }

    @MethodTypeHint(signature = "(this: any) -> item", documentation = "Returns the item associated with this event.")
    public static RItem item(REntityItemEvent event) {
        return event.secondary;
    }
}
