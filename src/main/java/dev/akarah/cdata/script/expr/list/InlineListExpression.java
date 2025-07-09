package dev.akarah.cdata.script.expr.list;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public record InlineListExpression(
        List<Expression> expressions
) implements Expression {
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
        for(var expr : expressions) {
            ctx
                    .dup()
                    .pushValue(expr)
                    .invokeVirtual(
                            CodegenUtil.ofClass(ArrayList.class),
                            "add",
                            MethodTypeDesc.of(
                                    CodegenUtil.ofBoolean(),
                                    List.of(CodegenUtil.ofClass(Object.class))
                            )
                    )
                    .bytecodeUnsafe(CodeBuilder::pop);
        }
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        try {
            return Type.list(expressions.getFirst().type(ctx));
        } catch (NoSuchElementException ignored) {
            return Type.list(Type.any());
        }
    }
}
