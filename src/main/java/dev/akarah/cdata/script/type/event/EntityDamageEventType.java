package dev.akarah.cdata.script.type.event;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.event.REntityDamageEvent;
import dev.akarah.cdata.script.value.event.REntityItemEvent;

import java.lang.constant.ClassDesc;

public record EntityDamageEventType(String name) implements Type<REntityDamageEvent>, EventType {
    @Override
    public String typeName() {
        return "event[" + name + "]";
    }

    @Override
    public Class<REntityDamageEvent> typeClass() {
        return REntityDamageEvent.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(REntityDamageEvent.class);
    }

    @Override
    public String defaultName() {
        return "entity_dmg";
    }
}
