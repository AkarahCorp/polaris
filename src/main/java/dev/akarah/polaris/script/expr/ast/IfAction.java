package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RBoolean;

import java.lang.classfile.Opcode;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;

public record IfAction(
        Expression condition,
        Expression then,
        Optional<Expression> orElse,
        SpanData span
) implements Expression {

    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.condition)
                .typecheck(RBoolean.class)
                .invokeVirtual(
                        CodegenUtil.ofClass(RBoolean.class),
                        "asInt",
                        MethodTypeDesc.of(
                                CodegenUtil.ofInt(),
                                List.of()
                        )
                );
        var exitLabel = ctx.bytecodeUnsafe().newLabel();
        ctx
                .pushFrame(exitLabel, ctx.getFrame().breakLabel())
                .ifThenElse(
                        Opcode.IFNE,
                        () -> ctx.pushValue(this.then),
                        () -> orElse.map(ctx::pushValue).orElse(ctx)
                )
                .popFrame()
                .bytecodeUnsafe(cb -> cb.labelBinding(exitLabel));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
