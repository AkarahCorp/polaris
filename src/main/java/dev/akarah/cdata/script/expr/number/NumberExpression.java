package dev.akarah.cdata.script.expr.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record NumberExpression(double value) implements Expression {
    public static MapCodec<NumberExpression> GENERATOR_CODEC = Codec.DOUBLE.fieldOf("value").xmap(NumberExpression::new, NumberExpression::value);

    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb -> cb.loadConstant(this.value));
    }

    @Override
    public Type<?> type() {
        return Type.number();
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
