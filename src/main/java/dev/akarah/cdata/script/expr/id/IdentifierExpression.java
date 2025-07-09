package dev.akarah.cdata.script.expr.id;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record IdentifierExpression(
        Expression namespace,
        Expression path
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(namespace)
                .typecheck(String.class)
                .pushValue(path)
                .typecheck(String.class)
                .invokeStatic(
                        CodegenUtil.ofClass(ResourceLocationUtil.class),
                        "create",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(ResourceLocation.class),
                                List.of(CodegenUtil.ofClass(String.class), CodegenUtil.ofClass(String.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("namespace", Type.string())
                .required("path", Type.string())
                .returns(Type.identifier())
                .build();
    }
}
