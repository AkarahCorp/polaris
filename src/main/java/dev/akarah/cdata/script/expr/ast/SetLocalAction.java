package dev.akarah.cdata.script.expr.ast;

import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.Optional;

public record SetLocalAction(
        String variable,
        Optional<Type<?>> typeHint,
        Expression value,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        if(typeHint.isPresent()) {
            if(!typeHint.orElseThrow().typeEquals(ctx.getTypeOf(value))) {
                throw new ParsingException("Type hint and value type do not match up", span);
            }
        }
        ctx.pushValue(this.value)
                .storeLocal(this.variable, typeHint.orElseGet(() -> ctx.getTypeOf(this.value)));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
