package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.exception.SpanData;

import java.lang.constant.ClassDesc;
import java.util.List;

public record SpannedType<T>(
    Type<T> type,
    SpanData span
) implements Type<T> {

    @Override
    public String typeName() {
        return this.type.typeName();
    }

    @Override
    public Class<T> typeClass() {
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

    @Override
    public String verboseTypeName() {
        return this.type.verboseTypeName();
    }
}
