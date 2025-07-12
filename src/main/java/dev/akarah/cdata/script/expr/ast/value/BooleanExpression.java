package dev.akarah.cdata.script.expr.ast.value;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record BooleanExpression(boolean value) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(value ? 1 : 0);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.bool();
    }
}
