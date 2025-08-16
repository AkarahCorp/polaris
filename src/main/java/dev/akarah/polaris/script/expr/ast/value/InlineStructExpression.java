package dev.akarah.polaris.script.expr.ast.value;

import com.mojang.datafixers.util.Pair;
import dev.akarah.polaris.script.exception.ParsingException;
import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.SpannedType;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RStruct;
import dev.akarah.polaris.script.value.RuntimeValue;

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
        // todo: add field validation checks


        var type = ctx.userTypes.get(name.replace(".", "_"));
        if(type == null) {
            throw new ParsingException("Type `" + name + "` does not exist.", this.span);
        }

        for(var expr : expressions) {
            for(var field : type.fields()) {
                if(field.name().equals(expr.getFirst())) {
                    if(!ctx.getTypeOf(expr.getSecond()).typeEquals(field.type())) {
                        throw new ParsingException(
                                "Expected type of `" + field.type() + "`, got `" + ctx.getTypeOf(expr.getSecond()) + "`",
                                expr.getSecond().span()
                        );
                    }
                }
            }
        }

        var structFields = expressions.stream().map(Pair::getFirst).toList();
        for(var field : type.fields()) {
            if(!structFields.contains(field.name()) && field.fallback() == null) {
                throw new ParsingException("Struct is missing field `" + field.name() + "`", this.span);
            }
        }

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
                this.span()
        );
    }
}
