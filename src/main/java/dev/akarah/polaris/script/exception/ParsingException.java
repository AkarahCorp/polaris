package dev.akarah.polaris.script.exception;

public class ParsingException extends SpannedException {
    public ParsingException(String message, SpanData span) {
        super(message, span);
    }
}
