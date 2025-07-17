package dev.akarah.cdata.script.value.event;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.mc.RItem;

public class RItemEvent extends REvent {
    private final RItem secondary;

    private RItemEvent(RItem secondary) {
        this.secondary = secondary;
    }

    public static RItemEvent of(RItem secondary) {
        return new RItemEvent(secondary);
    }

    @Override
    public Void javaValue() {
        throw new RuntimeException("Not applicable to RItemEvent.");
    }

    @MethodTypeHint(signature = "(this: any) -> item", documentation = "Gets the item associated with this event.")
    public static RItem item(RItemEvent event) {
        return event.secondary;
    }
}
