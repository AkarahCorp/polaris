package dev.akarah.cdata.script.expr.string;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.List;
import java.util.Optional;

public record StringExpression(String value) implements Expression {
    public static MapCodec<StringExpression> GENERATOR_CODEC = Codec.STRING.fieldOf("value").xmap(StringExpression::new, StringExpression::value);

    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb -> cb.loadConstant(this.value));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.string();
    }
}
