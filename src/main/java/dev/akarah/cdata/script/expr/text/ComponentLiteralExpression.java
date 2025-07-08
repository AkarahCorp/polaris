package dev.akarah.cdata.script.expr.text;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.MutableComponent;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record ComponentLiteralExpression(String value) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb -> cb.loadConstant(this.value).invokestatic(
                CodegenUtil.ofClass(TextUtil.class),
                "loreFriendlyLiteral",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(MutableComponent.class),
                        List.of(CodegenUtil.ofClass(String.class))
                )
        ));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.text();
    }
}
