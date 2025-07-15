package dev.akarah.cdata.script.expr.ast.value;

import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RList;
import dev.akarah.cdata.script.value.RuntimeValue;

import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.NoSuchElementException;

public record InlineListExpression(
        List<Expression> expressions,
        SpanData span
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
                    .invokeStatic(
                            CodegenUtil.ofClass(RList.class),
                            "add",
                            MethodTypeDesc.of(
                                    CodegenUtil.ofVoid(),
                                    List.of(
                                            CodegenUtil.ofClass(RList.class),
                                            CodegenUtil.ofClass(RuntimeValue.class)
                                    )
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
