package dev.akarah.cdata.script.expr;

import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record SpannedExpression<E extends Expression>(
        E expression,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.expression());
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return ctx.getTypeOf(this.expression());
    }
}
