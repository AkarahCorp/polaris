package dev.akarah.polaris.script.expr.ast.value;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.JavaClassType;
import dev.akarah.polaris.script.type.Type;

import java.lang.constant.ConstantDesc;

public record CdExpression(ConstantDesc cd) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(this.cd);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return new JavaClassType<>(cd.getClass(), cd.getClass().getName());
    }
}
