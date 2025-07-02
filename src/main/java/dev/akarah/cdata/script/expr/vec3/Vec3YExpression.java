package dev.akarah.cdata.script.expr.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record Vec3YExpression(
        Expression value
) implements Expression {
    public static MapCodec<Vec3YExpression> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Expression.codecByType(Type.vec3()).fieldOf("value").forGetter(Vec3YExpression::value)
    ).apply(instance, Vec3YExpression::new));

    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.value)
                .unboxNumber()
                .getVectorComponent("y");
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
