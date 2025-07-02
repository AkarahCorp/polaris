package dev.akarah.cdata.script.expr.flow;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record SetLocalAction(
        String variable,
        Expression value
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.value)
                .storeLocal(this.variable, ctx.getTypeOf(this.value));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
