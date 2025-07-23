package dev.akarah.cdata.script.expr.ast.value;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.SpannedType;
import dev.akarah.cdata.script.type.StructType;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RDict;
import dev.akarah.cdata.script.value.RStruct;
import dev.akarah.cdata.script.value.RuntimeValue;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record InlineStructExpression(
        String name,
        List<Pair<String, Expression>> expressions,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(this.name()).constant(this.expressions.size()).invokeStatic(
                CodegenUtil.ofClass(RStruct.class),
                "create",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(RStruct.class),
                        List.of(
                                CodegenUtil.ofClass(String.class),
                                CodegenUtil.ofInt()
                        )
                )
        );
        for(var expr : expressions) {
            ctx
                    .dup()
                    .constant(expr.getFirst())
                    .pushValue(expr.getSecond())
                    .invokeStatic(
                            CodegenUtil.ofClass(RStruct.class),
                            "put",
                            MethodTypeDesc.of(
                                    CodegenUtil.ofVoid(),
                                    List.of(
                                            CodegenUtil.ofClass(RStruct.class),
                                            CodegenUtil.ofClass(String.class),
                                            CodegenUtil.ofClass(RuntimeValue.class)
                                    )
                            )
                    );
        }
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        if(!ctx.userTypes.containsKey(this.name.replace(".", "_"))) {
            throw new ParsingException("Structure `" + this.name + "` does not exist!", this.span);
        }
        return new SpannedType<>(
                ctx.userTypes.get(this.name.replace(".", "_")),
                this.span(),
                this.name,
                this.name
        );
    }
}
