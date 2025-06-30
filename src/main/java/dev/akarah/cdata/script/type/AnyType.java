package dev.akarah.cdata.script.type;


public record AnyType() implements Type<Object> {
    @Override
    public String typeName() {
        return "any";
    }

    @Override
    public Class<Object> typeClass() {
        return Object.class;
    }
}
