package dev.akarah.cdata.script.expr.flow;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.Optional;

public record IfAction(
        Expression condition,
        Expression then,
        Optional<Expression> orElse
) implements Expression {
    public static MapCodec<IfAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Expression.codecByType(Type.bool()).fieldOf("if").forGetter(IfAction::condition),
            Expression.codecByType(Type.void_()).fieldOf("then").forGetter(IfAction::then),
            Expression.codecByType(Type.void_()).optionalFieldOf("else").forGetter(IfAction::orElse)
    ).apply(instance, IfAction::new));

    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.condition);
        ctx.ifThenElse(
                () -> ctx.pushValue(this.then),
                () -> orElse.map(ctx::pushValue).orElse(ctx)
        );
    }

    @Override
    public Type<?> type() {
        return Type.void_();
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
