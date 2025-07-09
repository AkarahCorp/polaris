package dev.akarah.cdata.script.expr.list;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.vec3.Vec3Util;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record NumberRangeExpression(
        Expression lhs,
        Expression rhs
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.lhs)
                .typecheck(Double.class)
                .pushValue(this.rhs)
                .typecheck(Double.class)
                .invokeStatic(
                        CodegenUtil.ofClass(ListUtil.class),
                        "range",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(List.class),
                                List.of(CodegenUtil.ofClass(Double.class), CodegenUtil.ofClass(Double.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("min", Type.number())
                .required("max", Type.number())
                .returns(Type.list(Type.number()))
                .build();
    }
}
