package dev.akarah.polaris.script.expr.ast.value;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;

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
