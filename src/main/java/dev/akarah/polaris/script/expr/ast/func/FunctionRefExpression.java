package dev.akarah.polaris.script.expr.ast.func;

import com.google.common.collect.Lists;
import dev.akarah.polaris.script.exception.ParsingException;
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

import javax.xml.validation.Schema;
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
    public SchemaExpression lambdaConversion(CodegenContext ctx) {
        for(var pair : ctx.schemas) {
            if(pair.getFirst().equals(this.identifier)) {
                return pair.getSecond();
            }
        }
        throw new ParsingException("Can not find schema `" + this.identifier + "`", this.span());
    }

    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(MethodHandleDesc.of(
                DirectMethodHandleDesc.Kind.STATIC,
                CodegenContext.ACTION_CLASS_DESC,
                CodegenContext.resourceLocationToMethodName(this.identifier),
                this.lambdaConversion(ctx).methodType().descriptorString()
        ));
        ctx.invokeStatic(
                CodegenUtil.ofClass(RFunction.class),
                "of",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(RFunction.class),
                        List.of(CodegenUtil.ofClass(MethodHandle.class))
                )
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.function(
                this.lambdaConversion(ctx).typeSet().returns(),
                this.lambdaConversion(ctx).typeSet().parameters().stream().map(ParameterNode::typePattern).toList()
        );
    }

    @Override
    public SpanData span() {
        return this.span;
    }
}