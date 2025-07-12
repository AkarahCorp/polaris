package dev.akarah.cdata.script.expr.ast.operation;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;

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
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.bool();
    }
}
