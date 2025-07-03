package dev.akarah.cdata.script.expr.vec3;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record Vec3YExpression(
        Expression value
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.value)
                .typecheck(Vec3.class)
                .getVectorComponent("y")
                .boxNumber();
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
