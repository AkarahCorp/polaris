package dev.akarah.cdata.script.expr.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record TextExpression(String value) implements Expression {
    public static MapCodec<TextExpression> GENERATOR_CODEC = Codec.STRING.fieldOf("value")
            .xmap(TextExpression::new, TextExpression::value);

    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb ->
                cb.loadConstant(this.value)
                        .invokestatic(
                                JIT.ofClass(Component.class),
                                "literal",
                                MethodTypeDesc.of(
                                        JIT.ofClass(MutableComponent.class),
                                        List.of(JIT.ofClass(String.class))
                                ),
                                true
                        )
        );
    }

    @Override
    public Type<?> type() {
        return Type.text();
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
