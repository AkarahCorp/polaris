package dev.akarah.cdata.script.value;

public class RString extends RuntimeValue<String> {
    private final String inner;

    private RString(String inner) {
        this.inner = inner;
    }

    public static RString of(String value) {
        return new RString(value);
    }

    @Override
    public String javaValue() {
        return this.inner;
    }
}
