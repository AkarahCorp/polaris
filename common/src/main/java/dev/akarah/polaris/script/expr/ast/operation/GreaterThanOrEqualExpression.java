package dev.akarah.polaris.script.expr.ast.operation;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RBoolean;
import dev.akarah.polaris.script.value.RNumber;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Opcode;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record GreaterThanOrEqualExpression(
        Expression lhs,
        Expression rhs,
        SpanData span
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
                        Opcode.IFGE,
                        () -> ctx.constant(1),
                        () -> ctx.constant(0)
                )
                .invokeStatic(
                        CodegenUtil.ofClass(RBoolean.class),
                        "of",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(RBoolean.class),
                                List.of(CodegenUtil.ofBoolean())
                        )
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.bool();
    }
}
