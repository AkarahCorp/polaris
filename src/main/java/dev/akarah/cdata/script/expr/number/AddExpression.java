package dev.akarah.cdata.script.expr.number;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;

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
                        CodegenUtil.ofClass(NumberUtil.class),
                        "add",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Object.class),
                                List.of(CodegenUtil.ofClass(Object.class), CodegenUtil.ofClass(Object.class))
                        )
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return ctx.getTypeOf(this.lhs).or(ctx.getTypeOf(this.rhs));
    }
}
