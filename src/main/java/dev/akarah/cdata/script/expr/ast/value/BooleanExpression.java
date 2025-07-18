package dev.akarah.cdata.script.expr.ast.value;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RBoolean;

public record BooleanExpression(boolean value) implements Expression {
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
