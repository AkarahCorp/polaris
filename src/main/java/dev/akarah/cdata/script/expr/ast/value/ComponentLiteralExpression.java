package dev.akarah.cdata.script.expr.ast.value;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.text.TextUtil;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.MutableComponent;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record ComponentLiteralExpression(String value) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(this.value).invokeStatic(
                CodegenUtil.ofClass(TextUtil.class),
                "loreFriendlyLiteral",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(MutableComponent.class),
                        List.of(CodegenUtil.ofClass(String.class))
                )
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.text();
    }
}
