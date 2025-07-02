package dev.akarah.cdata.script.expr.text;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.List;
import java.util.Optional;

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
    public Type<?> type(CodegenContext ctx) {
        return Type.text();
    }
}
