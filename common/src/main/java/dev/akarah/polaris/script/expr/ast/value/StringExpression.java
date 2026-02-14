package dev.akarah.polaris.script.expr.ast.value;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RString;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record StringExpression(String value, SpanData span) implements Expression {
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
