package dev.akarah.cdata.script.expr.flow;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record GetLocalAction(
        String variable
) implements Expression {
    public static MapCodec<GetLocalAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("variable").forGetter(GetLocalAction::variable)
    ).apply(instance, GetLocalAction::new));

    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushLocal(this.variable);

    }

    @Override
    public Type<?> type() {
        return Type.any();
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
