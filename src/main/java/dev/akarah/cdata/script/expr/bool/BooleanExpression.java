package dev.akarah.cdata.script.expr.bool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record BooleanExpression(boolean value) implements Expression {
    public static MapCodec<BooleanExpression> GENERATOR_CODEC = Codec.BOOL.fieldOf("value").xmap(BooleanExpression::new, BooleanExpression::value);

    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb -> cb.loadConstant(value ? 1 : 0));
    }

    @Override
    public Type<?> type() {
        return Type.bool();
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
