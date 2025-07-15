package dev.akarah.cdata.script.type.event;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.event.RDoubleEntityEvent;
import dev.akarah.cdata.script.value.event.REntityEvent;

import java.lang.constant.ClassDesc;

public record DoubleEntityEventType(String name) implements Type<RDoubleEntityEvent>, EventType {
    @Override
    public String typeName() {
        return "event[" + name + "]";
    }

    @Override
    public Class<RDoubleEntityEvent> typeClass() {
        return RDoubleEntityEvent.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RDoubleEntityEvent.class);
    }

    @Override
    public String defaultName() {
        return "entity2";
    }
}
