package dev.akarah.cdata.script.expr.ast.value;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.JavaClassType;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.ConstantDesc;

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
