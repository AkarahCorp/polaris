package dev.akarah.cdata.script.value.event;

import dev.akarah.cdata.script.value.RBoolean;
import dev.akarah.cdata.script.value.RuntimeValue;
import dev.akarah.cdata.script.value.mc.REntity;

public class REvent extends RuntimeValue {

    public REvent() {

    }

    public static REvent of() {
        return new REvent();
    }

    @Override
    public Void javaValue() {
        return null;
    }
}
