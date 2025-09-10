package dev.akarah.polaris.script.expr.ast.value;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.GlobalNamespace;
import dev.akarah.polaris.script.value.RText;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record ComponentLiteralExpression(String value, SpanData span) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(this.value).invokeStatic(
                CodegenUtil.ofClass(GlobalNamespace.class),
                "textLiteralInternal",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(RText.class),
                        List.of(CodegenUtil.ofClass(Object.class))
                )
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.text();
    }
}
