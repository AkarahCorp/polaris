package dev.akarah.polaris.script.expr;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public @NotNull String toString() {
        return this.expression.toString();
    }
}
