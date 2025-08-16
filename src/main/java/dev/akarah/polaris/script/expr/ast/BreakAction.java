package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;

public record BreakAction() implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecodeUnsafe(cb -> cb.goto_(ctx.getFrame().breakLabel()));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
