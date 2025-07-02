package dev.akarah.cdata.script.type;

public record BooleanType() implements Type<Boolean> {
    @Override
    public String typeName() {
        return "boolean";
    }

    @Override
    public Class<Boolean> typeClass() {
        return Boolean.class;
    }
}
