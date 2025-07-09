package dev.akarah.cdata.script.expr.vec3;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record Vec3ZExpression(
        Expression value
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.value)
                .typecheck(Vec3.class)
                .invokeStatic(
                        CodegenUtil.ofClass(Vec3Util.class),
                        "z",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Double.class),
                                List.of(CodegenUtil.ofClass(Vec3.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("vec", Type.vec3())
                .returns(Type.number())
                .build();
    }
}
