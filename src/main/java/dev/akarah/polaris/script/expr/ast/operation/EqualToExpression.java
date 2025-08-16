package dev.akarah.polaris.script.expr.ast.operation;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RBoolean;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record EqualToExpression(
        Expression lhs,
        Expression rhs
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
