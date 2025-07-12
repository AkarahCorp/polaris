package dev.akarah.cdata.script.expr.ast.value;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RString;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record StringExpression(String value) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(this.value)
                .invokeStatic(
                        CodegenUtil.ofClass(RString.class),
                        "of",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(RString.class),
                                List.of(CodegenUtil.ofClass(String.class))
                        )
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.string();
    }
}
