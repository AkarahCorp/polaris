package dev.akarah.cdata.script.expr.ast.func;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.ast.AllOfAction;
import dev.akarah.cdata.script.expr.ast.SchemaExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.params.ParameterNode;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RFunction;
import dev.akarah.cdata.script.value.RuntimeValue;

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

public record LambdaExpression(
        ExpressionTypeSet typeSet,
        AllOfAction body,
        SpanData keywordSpan
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.constant(MethodHandleDesc.of(
                DirectMethodHandleDesc.Kind.STATIC,
                CodegenContext.ACTION_CLASS_DESC,
                this.name(),
                this.methodType(ctx).descriptorString()
        ));
        var lastHighestLocal = ctx.highestLocal();
        for(int i = 0; i <= lastHighestLocal; i++) {
            ctx.aload(i).invokeVirtual(
                    CodegenUtil.ofClass(MethodHandle.class),
                    "bindTo",
                    MethodTypeDesc.of(
                            CodegenUtil.ofClass(MethodHandle.class),
                            List.of(CodegenUtil.ofClass(Object.class))
                    )
            );
        }
        ctx.invokeStatic(
                CodegenUtil.ofClass(RFunction.class),
                "of",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(RFunction.class),
                        List.of(CodegenUtil.ofClass(MethodHandle.class))
                )
        );
        ctx.requestAction(
                this.name(),
                this.asSchema(),
                lastHighestLocal,
                Lists.newArrayList(ctx.getFrames())
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.function(
                this.typeSet().returns(),
                this.typeSet().parameters().stream().map(ParameterNode::typePattern).toList()
        );
    }

    public MethodType methodType(CodegenContext ctx) {
        if(ctx.highestLocal() == -1) {
            return MethodType.methodType(
                    this.typeSet().returns().typeClass(),
                    this.typeSet().parameters().stream()
                            .map(ParameterNode::typePattern)
                            .map(Type::typeClass)
                            .toArray(Class[]::new)
            );
        }
        return MethodType.methodType(
                this.typeSet().returns().typeClass(),
                Stream.concat(
                        IntStream.rangeClosed(0, ctx.highestLocal())
                                .mapToObj(_ -> Object.class),
                        this.typeSet().parameters().stream()
                                .map(ParameterNode::typePattern)
                                .map(Type::typeClass)
                )
                        .toArray(Class[]::new)
        );
    }

    public String name() {
        var hash = Objects.hash(
                this.typeSet(),
                this.body
        );
        var hash2 = Objects.hash(
                this.body,
                this.typeSet()
        );
        return "fn_" + Math.abs(hash) + "$" + Math.abs(hash * hash2);
    }

    public SchemaExpression asSchema() {
        return new SchemaExpression(
                this.typeSet(),
                this.body,
                Optional.empty(),
                this.keywordSpan
        );
    }
}
