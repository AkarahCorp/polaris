package dev.akarah.cdata.script.expr.bool;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.List;
import java.util.Optional;

public record BooleanExpression(boolean value) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb -> cb.loadConstant(value ? 1 : 0));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.bool();
    }

    public static Optional<List<Pair<String, Type<?>>>> fields() {
        return Optional.empty();
    }
}
