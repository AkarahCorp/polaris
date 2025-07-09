package dev.akarah.cdata.script.expr.list;

import com.google.common.collect.Lists;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

public record CreateListExpression() implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.invokeStatic(
                CodegenUtil.ofClass(Lists.class),
                "newArrayList",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(ArrayList.class),
                        List.of()
                )
        );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .returns(Type.list(Type.any()))
                .build();
    }
}
