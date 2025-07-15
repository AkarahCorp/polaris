package dev.akarah.cdata.script.expr.ast;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record ContinueAction() implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecodeUnsafe(cb -> cb.goto_(ctx.getFrame().startLabel()));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
