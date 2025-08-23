package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.expr.ast.func.LambdaExpression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.params.ExpressionTypeSet;
import dev.akarah.polaris.script.params.ParameterNode;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.type.VoidType;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodType;
import java.util.Optional;

public record SchemaExpression(
        ExpressionTypeSet typeSet,
        AllOfAction body,
        Optional<String> eventName,
        SpanData keywordSpan,
        ResourceLocation location
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
