package dev.akarah.cdata.script.expr.ast.func;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record JvmFunctionAction(
        ClassDesc declaringClass,
        String name,
        MethodTypeDesc methodTypeDesc,
        List<Expression> parameters,
        Type<?> returnType
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        for(var expr : parameters) {
            ctx.pushValue(expr);
        }
        ctx.invokeStatic(
                declaringClass,
                name,
                methodTypeDesc
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return returnType;
    }
}
