package dev.akarah.cdata.script.type.event;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.event.REmptyEvent;
import dev.akarah.cdata.script.value.event.REntityEvent;

import java.lang.constant.ClassDesc;

public record EmptyEventType(String name) implements Type<REmptyEvent>, EventType {
    @Override
    public String typeName() {
        return "event[" + name + "]";
    }

    @Override
    public Class<REmptyEvent> typeClass() {
        return REmptyEvent.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(REmptyEvent.class);
    }

    @Override
    public String defaultName() {
        return "empty_event";
    }
}
