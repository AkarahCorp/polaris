package dev.akarah.cdata.script.type.event;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.event.RDoubleEntityEvent;
import dev.akarah.cdata.script.value.event.REntityItemEvent;

import java.lang.constant.ClassDesc;

public record EntityItemEventType(String name) implements Type<REntityItemEvent>, EventType {
    @Override
    public String typeName() {
        return "event[" + name + "]";
    }

    @Override
    public Class<REntityItemEvent> typeClass() {
        return REntityItemEvent.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(REntityItemEvent.class);
    }

    @Override
    public String defaultName() {
        return "entity_item";
    }
}
