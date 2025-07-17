package dev.akarah.cdata.script.value;

public class RBoolean extends RuntimeValue {
    private final boolean inner;

    private RBoolean(boolean inner) {
        this.inner = inner;
    }

    public static RBoolean of(boolean value) {
        return new RBoolean(value);
    }

    @Override
    public Boolean javaValue() {
        return this.inner;
    }

    public int asInt() {
        return this.inner ? 1 : 0;
    }
}
