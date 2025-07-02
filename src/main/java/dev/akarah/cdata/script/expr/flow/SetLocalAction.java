package dev.akarah.cdata.script.expr.flow;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.Optional;

public record SetLocalAction(
        String variable,
        Expression value
) implements Expression {
    public static MapCodec<SetLocalAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("variable").forGetter(SetLocalAction::variable),
            Expression.codecByType(Type.any()).fieldOf("value").forGetter(SetLocalAction::value)
    ).apply(instance, SetLocalAction::new));

    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.value)
                .storeLocal(this.variable);

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
