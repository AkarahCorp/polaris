package dev.akarah.cdata.script.value;

public abstract class RuntimeValue<T> {
    public abstract T javaValue();

    public static RDict dict() {
        return RDict.create();
    }

    public static RList list() {
        return RList.create();
    }

    public static RNumber number(double value) {
        return RNumber.of(value);
    }

    @Override
    public String toString() {
        return this.javaValue().toString();
    }

    public static RuntimeValue<?> fromJava(Object object) {
        return switch (object) {
            case String s -> RString.of(s);
            case Boolean b -> RBoolean.of(b);
            default -> null;
        };
    }

    @Override
    public int hashCode() {
        return this.javaValue().hashCode();
    }
}
