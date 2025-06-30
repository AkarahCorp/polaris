package dev.akarah.cdata.script.type;

public record StringType() implements Type<String> {
    @Override
    public String typeName() {
        return "string";
    }

    @Override
    public Class<String> typeClass() {
        return String.class;
    }
}
