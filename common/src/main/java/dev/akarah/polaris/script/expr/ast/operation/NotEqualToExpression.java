package dev.akarah.polaris.script.expr.ast.operation;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RBoolean;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record NotEqualToExpression(
        Expression lhs,
        Expression rhs,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(lhs)
                .pushValue(rhs)
                .invokeVirtual(
                        CodegenUtil.ofClass(Object.class),
                        "equals",
                        MethodTypeDesc.of(
                                CodegenUtil.ofBoolean(),
                                List.of(CodegenUtil.ofClass(Object.class))
                        )
                )
                .bytecodeUnsafe(CodeBuilder::iconst_1)
                .bytecodeUnsafe(CodeBuilder::ixor)
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
