package dev.akarah.cdata.script.expr.entity;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record EntityDirectionExpression(
        Expression entityExpression
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this)
                .typecheck(Entity.class)
                .invokeStatic(
                        CodegenUtil.ofClass(EntityUtil.class),
                        "entityDirection",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Vec3.class),
                                List.of(CodegenUtil.ofClass(Entity.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("entity", Type.entity())
                .returns(Type.vec3())
                .build();
    }
}
