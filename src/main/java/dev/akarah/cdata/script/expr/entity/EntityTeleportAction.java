package dev.akarah.cdata.script.expr.entity;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.lang.classfile.TypeKind;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record EntityTeleportAction(
        Expression entityExpression,
        Expression position
) implements Expression {

    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.entityExpression)
                .typecheck(Entity.class)
                .pushValue(this.position)
                .typecheck(Vec3.class)
                .invokeStatic(
                        CodegenUtil.ofClass(EntityUtil.class),
                        "teleportTo",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(CodegenUtil.ofClass(Entity.class), CodegenUtil.ofClass(Vec3.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("entity", Type.entity())
                .required("position", Type.vec3())
                .build();
    }
}
