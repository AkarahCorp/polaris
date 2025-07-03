package dev.akarah.cdata.script.exception;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

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
        sb.append("Multiple errors were encountered during parsing!");
        for(var exception : exceptions) {
            sb.append("\n\n ").append(exception.getMessage());
        }
        return sb.toString();
    }
}
