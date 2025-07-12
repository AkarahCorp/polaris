package dev.akarah.cdata.script.expr.dict;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RDict;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public record InlineDictExpression(
        List<Pair<Expression, Expression>> expressions
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.invokeStatic(
            CodegenUtil.ofClass(RDict.class),
            "create",
            MethodTypeDesc.of(
                    CodegenUtil.ofClass(RDict.class),
                    List.of()
            )
        );
        for(var expr : expressions) {
            ctx
                    .dup()
                    .pushValue(expr.getFirst())
                    .pushValue(expr.getSecond())
                    .invokeVirtual(
                            CodegenUtil.ofClass(RDict.class),
                            "put",
                            MethodTypeDesc.of(
                                    CodegenUtil.ofClass(Object.class),
                                    List.of(CodegenUtil.ofClass(Object.class), CodegenUtil.ofClass(Object.class))
                            )
                    )
                    .bytecodeUnsafe(CodeBuilder::pop);
        }
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        try {
            var baseKeyType = expressions.getFirst().getFirst().type(ctx);
            var baseValueType = expressions.getFirst().getSecond().type(ctx);

            for(var expr : expressions) {
                if(!expr.getFirst().type(ctx).typeEquals(baseKeyType)) {
                    baseKeyType = Type.any();
                }
                if(!expr.getSecond().type(ctx).typeEquals(baseValueType)) {
                    baseValueType = Type.any();
                }
            }
            return Type.dict(baseKeyType, baseValueType);
        } catch (Exception ignored) {
            return Type.dict(Type.any(), Type.any());
        }
    }
}
