package dev.akarah.cdata.script.expr.vec3;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record Vec3MultiplyExpression(
        Expression lhs,
        Expression rhs
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.lhs)
                .typecheck(Vec3.class)
                .pushValue(this.rhs)
                .typecheck(Vec3.class)
                .invokeStatic(
                        CodegenUtil.ofClass(Vec3Util.class),
                        "add",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Vec3.class),
                                List.of(CodegenUtil.ofClass(Vec3.class), CodegenUtil.ofClass(Vec3.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("lhs", Type.vector())
                .required("rhs", Type.vector())
                .returns(Type.vector())
                .build();
    }
}
