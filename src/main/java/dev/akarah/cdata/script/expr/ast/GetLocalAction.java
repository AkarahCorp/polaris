package dev.akarah.cdata.script.expr.ast;

import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record GetLocalAction(
        String variable,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushLocal(this.variable);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return ctx.typeOfLocal(this.variable(), this.span);
    }

}
