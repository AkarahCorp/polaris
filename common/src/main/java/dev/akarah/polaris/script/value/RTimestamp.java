package dev.akarah.polaris.script.value;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;


public class RTimestamp extends RuntimeValue implements Comparable<RTimestamp> {
    private final Instant inner;


    public static String typeName() {
        return "timestamp";
    }

    public RTimestamp(Instant inner) {
        this.inner = inner;
    }

    public static RTimestamp of(Instant inner) {return new RTimestamp(inner);}

    public static RTimestamp of(RNumber number) {return new RTimestamp(Instant.ofEpochMilli((int) number.doubleValue() * 1000L));}

    public static RTimestamp of() {return new RTimestamp(Instant.now());}

    @Override
    public int compareTo(@NotNull RTimestamp other) {
        return inner.compareTo(other.inner);
    }

    @MethodTypeHint(signature = "(this: timestamp) -> number", documentation = "Returns the seconds since 1970, with millisecond precision.")
    public static RNumber in_epoch_seconds(RTimestamp $this) {
        return RNumber.of((double) $this.inner.toEpochMilli() / 1000.0);
    }

    @MethodTypeHint(signature = "(this: timestamp, seconds: number) -> timestamp", documentation = "Returns a new timestamp, with the time adjusted by a number of seconds.")
    public static RTimestamp adjust_seconds(RTimestamp $this, RNumber seconds) {
        return RTimestamp.of($this.inner.plusMillis((long) (seconds.doubleValue() * 1000L)));
    }

    @Override
    public Instant javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return RTimestamp.of(this.inner);
    }
}
