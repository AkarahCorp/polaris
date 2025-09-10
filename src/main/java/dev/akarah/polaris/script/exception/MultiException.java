package dev.akarah.polaris.script.exception;

import net.minecraft.resources.ResourceLocation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

public class MultiException extends SpannedException {
    List<SpannedException> exceptions;

    public MultiException(List<SpannedException> exceptions) {
        super("", new SpanData(0, 0, "?", ResourceLocation.withDefaultNamespace("error")));
        this.exceptions = exceptions;
        this.span = SpanData.merge(this.exceptions.getFirst().span, this.exceptions.getLast().span);
        this.exceptions = exceptions;
    }

    @Override
    public String getMessage() {
        var sb = new StringBuilder();
        for(var exception : exceptions) {
            if(Objects.equals(System.getenv("POLARIS_PRINT_STACKTRACE_ON_COMPILE_FAIL"), "1")) {
                var sw = new StringWriter();
                var pw = new PrintWriter(sw);
                exception.printStackTrace(pw);
                sb.append(sw);
            } else {
                sb.append("\n\n ").append(exception.getMessage());
            }
        }
        return sb.toString();
    }
}
