package dev.akarah.cdata.script.expr.ast;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.ast.func.LambdaExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.type.VoidType;
import dev.akarah.cdata.script.value.RuntimeValue;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;

public record SchemaExpression(
        List<Pair<String, Type<?>>> parameters,
        Type<?> returnType,
        AllOfAction body,
        Optional<String> eventName,
        SpanData keywordSpan
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        this.body().compile(ctx);
        if(!this.returnType.typeEquals(Type.void_())) {
            var result = this.body.validateReturnOnAllBranches(ctx, this.returnType);
            if (!result) {
                throw new ParsingException("Not all control flows have a return statement!", keywordSpan);
            }
        }
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return this.returnType;
    }

    public MethodType methodType() {
        return MethodType.methodType(
                this.returnType.flatten() instanceof VoidType ? void.class : RuntimeValue.class,
                this.parameters.stream()
                        .map(Pair::getSecond)
                        .map(Type::typeClass)
                        .toArray(Class[]::new)
        );
    }

    public LambdaExpression asLambdaExpression() {
        return new LambdaExpression(
                this.parameters,
                this.returnType,
                this.body,
                this.keywordSpan
        );
    }
}
