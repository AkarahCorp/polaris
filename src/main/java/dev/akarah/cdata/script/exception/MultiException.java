package dev.akarah.cdata.script.exception;

import java.util.List;

public class MultiException extends RuntimeException {
    List<Exception> exceptions;

    public MultiException(List<Exception> exceptions) {
        this.exceptions = exceptions;
    }

    @Override
    public String getMessage() {
        var sb = new StringBuilder();
        sb.append("Multiple errors were encountered during parsing!");
        for(var exception : exceptions) {
            sb.append("\n- ").append(exception.getMessage());
        }
        return sb.toString();
    }
}
