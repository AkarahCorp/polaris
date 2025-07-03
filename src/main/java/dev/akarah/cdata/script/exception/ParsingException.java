package dev.akarah.cdata.script.exception;

public class ParsingException extends RuntimeException {
    public ParsingException(String message, int cursor) {
        super(message + " at token " + cursor);
    }
}
