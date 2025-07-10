package dev.akarah.cdata.script.expr.entity;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record EntityStatExpression(
        Expression entity,
        Expression statKey
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.entity)
                .typecheck(Entity.class)
                .pushValue(this.statKey)
                .typecheck(String.class)
                .invokeStatic(
                        CodegenUtil.ofClass(EntityUtil.class),
                        "entityStat",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Double.class),
                                List.of(CodegenUtil.ofClass(Entity.class), CodegenUtil.ofClass(String.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("entity", Type.entity())
                .required("stat", Type.string())
                .returns(Type.number())
                .build();
    }
}
