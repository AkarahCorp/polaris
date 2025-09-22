package dev.akarah.polaris.script.expr.ast.func;

import com.google.common.collect.Lists;
import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.expr.ast.AllOfAction;
import dev.akarah.polaris.script.expr.ast.SchemaExpression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.params.ExpressionTypeSet;
import dev.akarah.polaris.script.params.ParameterNode;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RFunction;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.resources.ResourceLocation;

import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record FunctionRefExpression(
        ResourceLocation identifier,
        SpanData span
) implements Expression {
    public LambdaExpression lambdaConversion(CodegenContext ctx) {
        return ctx.actions.get(CodegenContext.resourceLocationToMethodName(this.identifier)).asLambdaExpression();
    }

    @Override
    public void compile(CodegenContext ctx) {
        lambdaConversion(ctx).compile(ctx);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return lambdaConversion(ctx).type(ctx);
    }

    @Override
    public SpanData span() {
        return this.span;
    }
}