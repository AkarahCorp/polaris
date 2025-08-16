package dev.akarah.polaris.script.value;

import java.util.UUID;

public class RUuid extends RuntimeValue {
    private final UUID inner;

    private RUuid(UUID inner) {
        this.inner = inner;
    }

    public static RUuid of(UUID value) {
        return new RUuid(value);
    }

    @Override
    public UUID javaValue() {
        return this.inner;
    }
}
