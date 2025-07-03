package dev.akarah.cdata.script.exception;

public class SpannedException extends RuntimeException {
     public SpannedException(String message, SpanData span) {
         super(message);
         this.span = span;
     }

     SpanData span;

     public SpanData span() {
         return this.span;
     }

    @Override
    public String getMessage() {
        return super.getMessage() +
                "\n" +
                span.debugInfo();
    }
}
