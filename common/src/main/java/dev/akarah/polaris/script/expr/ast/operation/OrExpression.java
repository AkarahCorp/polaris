package dev.akarah.polaris.script.expr.ast.operation;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RBoolean;
import dev.akarah.polaris.script.value.RuntimeValue;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record OrExpression(
        Expression lhs,
        Expression rhs,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(lhs)
                .typecheck(RBoolean.class)
                .pushValue(rhs)
                .typecheck(RBoolean.class)
                .invokeStatic(
                        CodegenUtil.ofClass(OperationUtil.class),
                        "or",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(RuntimeValue.class),
                                List.of(CodegenUtil.ofClass(RuntimeValue.class), CodegenUtil.ofClass(RuntimeValue.class))
                        )
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return ctx.getTypeOf(this.lhs).or(ctx.getTypeOf(this.rhs));
    }
}
