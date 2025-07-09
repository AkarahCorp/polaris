package dev.akarah.cdata.script.expr.list;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

public record AddListExpression(
        Expression listValue,
        Expression valueToPush
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(listValue)
                .typecheck(ArrayList.class)
                .pushValue(valueToPush)
                .invokeVirtual(
                        CodegenUtil.ofClass(ArrayList.class),
                        "add",
                        MethodTypeDesc.of(
                                CodegenUtil.ofBoolean(),
                                List.of(CodegenUtil.ofClass(Object.class))
                        )
                )
                .pop();
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("list", e -> Type.list(Type.var(e, "T")))
                .required("value", e -> Type.var(e, "T"))
                .build();
    }
}
