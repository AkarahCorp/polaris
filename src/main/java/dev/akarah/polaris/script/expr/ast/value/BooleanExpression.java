package dev.akarah.polaris.script.expr.ast.value;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RBoolean;

public record BooleanExpression(boolean value, SpanData span) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(value ? 1 : 0).invokeStatic(
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
