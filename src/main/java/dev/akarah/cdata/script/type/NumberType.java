package dev.akarah.cdata.script.type;

public record NumberType() implements Type<Double> {
    @Override
    public String typeName() {
        return "number";
    }

    @Override
    public Class<Double> typeClass() {
        return Double.class;
    }
}
