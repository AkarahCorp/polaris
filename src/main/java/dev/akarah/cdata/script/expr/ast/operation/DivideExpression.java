package dev.akarah.cdata.script.expr.ast.operation;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;

public record DivideExpression(
        Expression lhs,
        Expression rhs
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(lhs)
                .unboxNumber()
                .pushValue(rhs)
                .unboxNumber()
                .bytecodeUnsafe(CodeBuilder::ddiv)
                .boxNumber();
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.number();
    }
}
