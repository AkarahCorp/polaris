package dev.akarah.cdata.script.expr.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record TextExpression(ParsedText value) implements Expression {
    public static MapCodec<TextExpression> GENERATOR_CODEC = ParsedText.CODEC.fieldOf("value")
            .xmap(TextExpression::new, TextExpression::value);

    @Override
    public void compile(CodegenContext ctx) {
        var fieldName = ctx.randomName("player_send_message");
        ctx
                .createStatic(fieldName, ParsedText.class, this.value())
                .loadStatic(fieldName, ParsedText.class)
                .bytecode(cb -> cb);
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
