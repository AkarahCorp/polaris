package dev.akarah.cdata.script.expr.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record Vec3Expression(
        Expression x,
        Expression y,
        Expression z
) implements Expression {
    public static MapCodec<Vec3Expression> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Expression.codecByType(Type.number()).fieldOf("x").forGetter(Vec3Expression::x),
            Expression.codecByType(Type.number()).fieldOf("y").forGetter(Vec3Expression::y),
            Expression.codecByType(Type.number()).fieldOf("z").forGetter(Vec3Expression::z)
    ).apply(instance, Vec3Expression::new));

    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .bytecode(cb -> cb.new_(JIT.ofClass(Vec3.class)).dup())
                .pushValue(this.x)
                .pushValue(this.y)
                .pushValue(this.z)
                .bytecode(cb -> cb.invokespecial(
                        JIT.ofClass(Vec3.class),
                        "<init>",
                        MethodTypeDesc.of(
                                JIT.ofVoid(),
                                List.of(JIT.ofDouble(), JIT.ofDouble(), JIT.ofDouble()
                        )
                )));
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
