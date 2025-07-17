package dev.akarah.cdata.script.expr.ast.value;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.StructType;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RDict;
import dev.akarah.cdata.script.value.RStruct;
import dev.akarah.cdata.script.value.RuntimeValue;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record InlineStructExpression(
        List<Pair<String, Expression>> expressions,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(this.expressions.size()).invokeStatic(
                CodegenUtil.ofClass(RStruct.class),
                "create",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(RStruct.class),
                        List.of(
                                CodegenUtil.ofInt()
                        )
                )
        );
        int idx = 0;
        for(var expr : expressions) {
            ctx
                    .dup()
                    .constant(idx)
                    .pushValue(expr.getSecond())
                    .invokeStatic(
                            CodegenUtil.ofClass(RStruct.class),
                            "put",
                            MethodTypeDesc.of(
                                    CodegenUtil.ofVoid(),
                                    List.of(
                                            CodegenUtil.ofClass(RStruct.class),
                                            CodegenUtil.ofInt(),
                                            CodegenUtil.ofClass(RuntimeValue.class)
                                    )
                            )
                    );
            idx += 1;
        }
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.struct(
                this.expressions
                        .stream()
                        .map(x -> new StructType.Field(x.getFirst(), ctx.getTypeOf(x.getSecond())))
                        .toList()
        );
    }
}
