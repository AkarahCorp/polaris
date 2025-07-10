package dev.akarah.cdata.script.expr.dict;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.MethodTypeDesc;
import java.util.HashMap;
import java.util.List;

record CreateDictExpression() implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.invokeStatic(
                CodegenUtil.ofClass(Maps.class),
                "newHashMap",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(HashMap.class),
                        List.of()
                )
        );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .returns(Type.dict(Type.any(), Type.any()))
                .build();
    }
}
