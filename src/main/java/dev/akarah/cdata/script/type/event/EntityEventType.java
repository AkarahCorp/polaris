package dev.akarah.cdata.script.type.event;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RString;
import dev.akarah.cdata.script.value.event.REntityEvent;

import java.lang.constant.ClassDesc;

public record EntityEventType(String name) implements Type<REntityEvent>, EventType {
    @Override
    public String typeName() {
        return "event[" + name + "]";
    }

    @Override
    public Class<REntityEvent> typeClass() {
        return REntityEvent.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(REntityEvent.class);
    }

    @Override
    public String defaultName() {
        return "entity";
    }
}
