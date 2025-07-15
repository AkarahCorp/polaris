package dev.akarah.cdata.script.expr.ast;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RBoolean;

import java.lang.classfile.Opcode;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;

public record IfAction(
        Expression condition,
        Expression then,
        Optional<Expression> orElse
) implements Expression {

    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.condition)
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
                .pushFrame(exitLabel, exitLabel)
                .ifThenElse(
                        Opcode.IFNE,
                        () -> ctx.pushValue(this.then),
                        () -> orElse.map(ctx::pushValue).orElse(ctx)
                )
                .bytecodeUnsafe(cb -> cb.labelBinding(exitLabel));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
