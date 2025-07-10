package dev.akarah.cdata.script.expr.text;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.MutableComponent;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record ComponentColorExpression(
        Expression component,
        Expression color
) implements Expression {

    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.component)
                .pushValue(this.color)
                .invokeStatic(
                        CodegenUtil.ofClass(TextUtil.class),
                        "withColor",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(MutableComponent.class),
                                List.of(CodegenUtil.ofClass(MutableComponent.class), CodegenUtil.ofClass(String.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("text", Type.text())
                .required("color", Type.string())
                .returns(Type.text())
                .build();
    }
}
