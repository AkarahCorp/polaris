package dev.akarah.cdata.script.expr.list;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RList;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
record GetListExpression(
        Expression listValue,
        Expression index
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(listValue)
                .typecheck(RList.class)
                .pushValue(index)
                .typecheck(Double.class)
                .invokeVirtual(
                        CodegenUtil.ofClass(RList.class),
                        "get",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Object.class),
                                List.of(CodegenUtil.ofClass(Double.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("list", e -> Type.list(Type.var(e, "T")))
                .required("index", Type.number())
                .returns(e -> Type.var(e, "T"))
                .build();
    }
}
