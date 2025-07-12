package dev.akarah.cdata.script.expr.ast.operation;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RNumber;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Opcode;

public record LessThanExpression(
        Expression lhs,
        Expression rhs
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(lhs)
                .typecheck(RNumber.class)
                .unboxNumber()
                .pushValue(rhs)
                .typecheck(RNumber.class)
                .unboxNumber()
                .bytecodeUnsafe(CodeBuilder::dcmpg)
                .ifThenElse(
                        Opcode.IFLT,
                        () -> ctx.constant(1),
                        () -> ctx.constant(0)
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.bool();
    }
}
