package dev.akarah.cdata.script.type;

public record VoidType() implements Type<Void> {
    @Override
    public String typeName() {
        return "void";
    }

    @Override
    public Class<Void> typeClass() {
        return Void.class;
    }
}
