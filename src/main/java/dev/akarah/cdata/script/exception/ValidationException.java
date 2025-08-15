package dev.akarah.cdata.script.exception;

public class ValidationException extends SpannedException {
    public ValidationException(String message, SpanData span) {
        super(message, span);
    }
}
