package dev.akarah.cdata.script.expr.text;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record ComponentLiteralFuncExpression(Expression value) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.value)
                .invokeVirtual(
                        CodegenUtil.ofClass(Object.class),
                        "toString",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(String.class),
                                List.of()
                        )
                )
                .invokeStatic(
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

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("value", Type.any())
        );
    }
}
