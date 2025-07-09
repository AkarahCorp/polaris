package dev.akarah.cdata.script.expr.list;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

public record GetListExpression(
        Expression listValue,
        Expression index
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(listValue)
                .typecheck(ArrayList.class)
                .pushValue(index)
                .typecheck(Double.class)
                .unboxNumber()
                .d2i()
                .invokeVirtual(
                        CodegenUtil.ofClass(ArrayList.class),
                        "get",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Object.class),
                                List.of(CodegenUtil.ofInt())
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
