package dev.akarah.cdata.script.expr.ast;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.ast.func.LambdaExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.invoke.MethodType;
import java.util.List;

public record TypeExpression(
        Type<?> alias
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {

    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return this.alias;
    }
}
