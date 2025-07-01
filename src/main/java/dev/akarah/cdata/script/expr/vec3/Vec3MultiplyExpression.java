package dev.akarah.cdata.script.expr.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record Vec3MultiplyExpression(
        Expression lhs,
        Expression rhs
) implements Expression {
    public static MapCodec<Vec3MultiplyExpression> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Expression.codecByType(Type.vec3()).fieldOf("value").forGetter(Vec3MultiplyExpression::lhs),
            Expression.codecByType(Type.vec3()).fieldOf("rhs").forGetter(Vec3MultiplyExpression::rhs)
    ).apply(instance, Vec3MultiplyExpression::new));

    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.lhs)
                .pushValue(this.rhs)
                .bytecode(cb -> cb.invokevirtual(
                        JIT.ofClass(Vec3.class),
                        "multiply",
                        MethodTypeDesc.of(
                                JIT.ofClass(Vec3.class),
                                List.of(JIT.ofClass(Vec3.class))
                        )
                ));
    }

    @Override
    public Type<?> type() {
        return Type.vec3();
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
