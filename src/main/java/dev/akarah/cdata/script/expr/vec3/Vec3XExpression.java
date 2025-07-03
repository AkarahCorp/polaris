package dev.akarah.cdata.script.expr.vec3;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.List;
import java.util.Optional;

public record Vec3XExpression(
        Expression value
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.value)
                .getVectorComponent("x");
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.number();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("value", Type.vec3())
        );
    }
}
