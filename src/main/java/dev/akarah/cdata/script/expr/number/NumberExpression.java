package dev.akarah.cdata.script.expr.number;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record NumberExpression(double value) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(this.value).boxNumber();
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.number();
    }
}
