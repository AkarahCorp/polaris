package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;

import java.lang.classfile.CodeBuilder;

public record ReturnAction(Expression value, SpanData span) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        if(value == null) {
            ctx.bytecodeUnsafe(CodeBuilder::return_);
        } else {
            ctx
                    .pushValue(value)
                    .bytecodeUnsafe(CodeBuilder::areturn);
        }
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
