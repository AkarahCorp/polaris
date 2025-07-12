package dev.akarah.cdata.script.expr.ast.operation;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RuntimeValue;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record AddExpression(
        Expression lhs,
        Expression rhs
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(lhs)
                .pushValue(rhs)
                .invokeStatic(
                        CodegenUtil.ofClass(OperationUtil.class),
                        "add",
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
