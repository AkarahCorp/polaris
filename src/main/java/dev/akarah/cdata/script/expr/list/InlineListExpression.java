package dev.akarah.cdata.script.expr.list;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RList;

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
            CodegenUtil.ofClass(RList.class),
            "create",
            MethodTypeDesc.of(
                    CodegenUtil.ofClass(RList.class),
                    List.of()
            )
        );
        for(var expr : expressions) {
            ctx
                    .dup()
                    .pushValue(expr)
                    .invokeVirtual(
                            CodegenUtil.ofClass(RList.class),
                            "add",
                            MethodTypeDesc.of(
                                    CodegenUtil.ofVoid(),
                                    List.of(CodegenUtil.ofClass(Object.class))
                            )
                    );
        }
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        try {
            var baseSubType = expressions.getFirst().type(ctx);

            for(var expr : expressions) {
                if(!expr.type(ctx).typeEquals(baseSubType)) {
                    baseSubType = Type.any();
                }
            }
            return Type.list(baseSubType);
        } catch (NoSuchElementException ignored) {
            return Type.list(Type.any());
        }
    }
}
