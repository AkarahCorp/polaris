package dev.akarah.cdata.script.params;

import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;

import java.util.List;

public class ExpressionStream {
    List<Expression> parameters;
    int index = 0;
    SpanData fallbackSpan;

    private ExpressionStream() {

    }

    public static ExpressionStream of(List<Expression> expressions, SpanData fallbackSpan) {
        var s = new ExpressionStream();
        s.parameters = expressions;
        s.fallbackSpan = fallbackSpan;
        return s;
    }

    public Expression peek() {
        if(index >= this.parameters.size()) {
            return null;
        }
        return this.parameters.get(index);
    }

    public Expression read() {
        if(index >= this.parameters.size()) {
            return null;
        }
        return this.parameters.get(index++);
    }

    public SpanData span() {
        return this.fallbackSpan;
    }
}
