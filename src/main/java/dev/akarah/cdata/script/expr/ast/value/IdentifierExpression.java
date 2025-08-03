package dev.akarah.cdata.script.expr.ast.value;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.mc.RIdentifier;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record IdentifierExpression(String namespace, String path) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(this.namespace).constant(this.path)
                .invokeStatic(
                        CodegenUtil.ofClass(RIdentifier.class),
                        "of",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(RIdentifier.class),
                                List.of(CodegenUtil.ofClass(String.class), CodegenUtil.ofClass(String.class))
                        )
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.identifier();
    }
}
