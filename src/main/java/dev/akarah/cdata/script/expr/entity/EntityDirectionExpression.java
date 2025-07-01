package dev.akarah.cdata.script.expr.entity;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record EntityDirectionExpression() implements Expression {
    public static MapCodec<EntityDirectionExpression> GENERATOR_CODEC = MapCodec.unit(EntityDirectionExpression::new);

    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushSelectedEntity()
                .bytecode(cb -> cb.invokevirtual(
                        JIT.ofClass(Entity.class),
                        "getLookAngle",
                        MethodTypeDesc.of(
                                JIT.ofClass(Vec3.class),
                                List.of()
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
