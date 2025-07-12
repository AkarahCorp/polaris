package dev.akarah.cdata.script.expr.ast;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.Optional;

public record IfAction(
        Expression condition,
        Expression then,
        Optional<Expression> orElse
) implements Expression {

    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.condition);
        ctx.ifThenElse(
                () -> ctx.pushValue(this.then),
                () -> orElse.map(ctx::pushValue).orElse(ctx)
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
