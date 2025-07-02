package dev.akarah.cdata.script.expr.flow;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public record EndAction()implements Expression {
    @Override
    public void compile(CodegenContext ctx) {

    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
