package dev.akarah.cdata.script.expr.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;

public record EntityDirectionExpression(
        Expression entityExpression
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this)
                .typecheck(Entity.class)
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
    public Type<?> type(CodegenContext ctx) {
        return Type.vec3();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("entity", Type.entity())
        );
    }
}
