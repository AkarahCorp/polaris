package dev.akarah.cdata.script.expr.ast;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;

public record ReturnAction(Expression value) implements Expression {
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
