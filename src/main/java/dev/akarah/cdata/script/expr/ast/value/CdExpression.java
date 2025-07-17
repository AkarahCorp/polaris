package dev.akarah.cdata.script.expr.ast.value;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.JavaClassType;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RString;

import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

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
