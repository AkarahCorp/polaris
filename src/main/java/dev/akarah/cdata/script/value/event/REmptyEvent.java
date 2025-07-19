package dev.akarah.cdata.script.value.event;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.mc.REntity;

public class REmptyEvent extends REvent {
    public static REmptyEvent of() {
        return new REmptyEvent();
    }

    @Override
    public Void javaValue() {
        throw new RuntimeException("Not applicable to REmptyEvent.");
    }
}
