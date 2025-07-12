package dev.akarah.cdata.script.expr.ast.value;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.GlobalNamespace;
import dev.akarah.cdata.script.value.RText;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record ComponentLiteralExpression(String value) implements Expression {
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
