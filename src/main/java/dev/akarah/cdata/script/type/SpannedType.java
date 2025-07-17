package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.exception.SpanData;

import java.lang.constant.ClassDesc;
import java.util.List;

public record SpannedType<T>(
    Type<T> type,
    SpanData span,
    String renamed,
    String verboseRenaming
) implements Type<T> {
    public SpannedType(Type<T> type, SpanData span) {
        this(type, span, type.typeName(), type.verboseTypeName());
    }

    @Override
    public String typeName() {
        return this.renamed();
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
        return this.verboseRenaming;
    }

    public SpannedType<T> rename(String name, String verboseRename) {
        return new SpannedType<>(this.type(), this.span(), name, verboseRename);
    }
}
