package dev.akarah.cdata.script.value.event;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.mc.REntity;

public class REntityEvent extends REvent {
    private final REntity primary;

    private REntityEvent(REntity primary) {
        this.primary = primary;
    }

    public static REntityEvent of(REntity entity) {
        return new REntityEvent(entity);
    }

    @Override
    public Void javaValue() {
        throw new RuntimeException("Not applicable to REntityEvent.");
    }

    @MethodTypeHint(signature = "(this: any) -> entity", documentation = "Returns the entity associated with this event.")
    public static REntity entity(REntityEvent event) {
        return event.primary;
    }
}
