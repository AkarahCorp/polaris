package dev.akarah.cdata.script.expr.ast;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.exception.ValidationException;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.ast.func.LambdaExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.params.ParameterNode;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.type.VoidType;
import dev.akarah.cdata.script.value.RuntimeValue;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;

public record SchemaExpression(
        ExpressionTypeSet typeSet,
        AllOfAction body,
        Optional<String> eventName,
        SpanData keywordSpan
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        this.body().compile(ctx);

        this.body.validateReturnOnAllBranches(ctx, this.typeSet().returns());
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return this.typeSet().returns();
    }

    public MethodType methodType() {
        return MethodType.methodType(
                this.typeSet().returns().flatten() instanceof VoidType ? void.class : RuntimeValue.class,
                this.typeSet.parameters().stream()
                        .map(ParameterNode::typePattern)
                        .map(Type::typeClass)
                        .toArray(Class[]::new)
        );
    }

    public LambdaExpression asLambdaExpression() {
        return new LambdaExpression(
                this.typeSet(),
                this.body,
                this.keywordSpan
        );
    }
}
