package dev.akarah.cdata.script.expr.ast.value;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RDict;
import dev.akarah.cdata.script.value.RuntimeValue;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record InlineDictExpression(
        List<Pair<Expression, Expression>> expressions,
        SpanData span
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
                    .invokeStatic(
                            CodegenUtil.ofClass(RDict.class),
                            "put",
                            MethodTypeDesc.of(
                                    CodegenUtil.ofVoid(),
                                    List.of(
                                            CodegenUtil.ofClass(RDict.class),
                                            CodegenUtil.ofClass(RuntimeValue.class),
                                            CodegenUtil.ofClass(RuntimeValue.class)
                                    )
                            )
                    );
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
