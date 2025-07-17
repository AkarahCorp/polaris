package dev.akarah.cdata.script.expr.ast.func;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.ast.AllOfAction;
import dev.akarah.cdata.script.expr.ast.SchemaExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RFunction;

import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;

public record LambdaExpression(
        List<Pair<String, Type<?>>> parameters,
        Type<?> returnType,
        AllOfAction body
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .constant(MethodHandleDesc.of(
                    DirectMethodHandleDesc.Kind.STATIC,
                    CodegenContext.ACTION_CLASS_DESC,
                    this.name(),
                    this.methodType().descriptorString()
                ))
                .invokeStatic(
                        CodegenUtil.ofClass(RFunction.class),
                        "of",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(RFunction.class),
                                List.of(CodegenUtil.ofClass(MethodHandle.class))
                        )
                );
        ctx.requestAction(
                this.name(),
                this.asSchema()
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.function(
                this.returnType,
                this.parameters.stream().map(Pair::getSecond).toList()
        );
    }

    public MethodType methodType() {
        return MethodType.methodType(
                this.returnType.typeClass(),
                this.parameters.stream()
                        .map(Pair::getSecond)
                        .map(Type::typeClass)
                        .toArray(Class[]::new)
        );
    }

    public String name() {
        var hash = Objects.hash(
                this.parameters,
                this.returnType,
                this.body
        );
        var hash2 = Objects.hash(
                this.body,
                this.parameters,
                this.returnType
        );
        return "fn_" + Math.abs(hash) + "$" + Math.abs(hash * hash2);
    }

    public SchemaExpression asSchema() {
        return new SchemaExpression(
                this.parameters,
                this.returnType,
                this.body
        );
    }
}
