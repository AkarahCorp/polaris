package dev.akarah.cdata.script.expr.flow;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.List;
import java.util.Optional;

public record GetLocalAction(
        String variable
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushLocal(this.variable);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return ctx.typeOfLocal(this.variable());
    }
}
