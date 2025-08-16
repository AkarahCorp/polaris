package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;

public record GetLocalAction(
        String variable,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushLocal(this.variable, this.span);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return ctx.typeOfLocal(this.variable(), this.span);
    }

}
