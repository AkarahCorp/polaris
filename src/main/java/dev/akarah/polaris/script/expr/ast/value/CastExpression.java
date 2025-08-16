package dev.akarah.polaris.script.expr.ast.value;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;

public record CastExpression(
        Expression base,
        Type<?> cast
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(base);
        ctx.typecheck(cast.typeClass());
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return this.cast;
    }
}
