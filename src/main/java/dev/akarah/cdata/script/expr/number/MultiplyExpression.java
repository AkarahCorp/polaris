package dev.akarah.cdata.script.expr.number;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;

public record MultiplyExpression(
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
                .bytecode(CodeBuilder::dmul)
                .boxNumber();
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.number();
    }
}
