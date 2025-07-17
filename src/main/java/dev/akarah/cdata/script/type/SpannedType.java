package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.exception.SpanData;

import java.lang.constant.ClassDesc;
import java.util.List;

public record SpannedType<U,T extends Type<U>>(
    T type,
    SpanData span
) implements Type<U> {
    @Override
    public String typeName() {
        return this.type().typeName();
    }

    @Override
    public Class<U> typeClass() {
        return this.type().typeClass();
    }

    @Override
    public ClassDesc classDescType() {
        return this.type().classDescType();
    }

    @Override
    public List<? extends Type<?>> subtypes() {
        return this.type.subtypes();
    }
}
