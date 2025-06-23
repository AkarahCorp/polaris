package dev.akarah.cdata.registry.text;

public class SkipLineException extends RuntimeException {
    public SkipLineException(String message) {
        super(message);
    }

    public SkipLineException(String message, Throwable cause) {
        super(message, cause);
    }
}
