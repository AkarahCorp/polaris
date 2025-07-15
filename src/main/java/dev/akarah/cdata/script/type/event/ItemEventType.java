package dev.akarah.cdata.script.type.event;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.event.REntityItemEvent;
import dev.akarah.cdata.script.value.event.RItemEvent;

import java.lang.constant.ClassDesc;

public record ItemEventType(String name) implements Type<RItemEvent>, EventType {
    @Override
    public String typeName() {
        return "event[" + name + "]";
    }

    @Override
    public Class<RItemEvent> typeClass() {
        return RItemEvent.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RItemEvent.class);
    }

    @Override
    public String defaultName() {
        return "entity_item";
    }
}
