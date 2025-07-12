package dev.akarah.cdata.script.expr.ast;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record StringExpression(String value) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(this.value);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.string();
    }
}
