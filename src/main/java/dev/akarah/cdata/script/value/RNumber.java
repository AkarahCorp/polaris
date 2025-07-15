package dev.akarah.cdata.script.value;

public class RNumber extends RuntimeValue<Double> {
    private final double inner;

    private RNumber(double inner) {
        this.inner = inner;
    }

    public static RNumber of(double value) {
        return new RNumber(value);
    }

    @Override
    public Double javaValue() {
        return this.inner;
    }

    public double doubleValue() {
        return this.inner;
    }

    public int intValue() { return (int) this.inner; }

    @Override
    public String toString() {
        return Double.toString(this.inner);
    }
}
