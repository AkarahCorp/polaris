package dev.akarah.cdata.script.expr.flow;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.util.Optional;

public record RepeatTimesAction(
        Expression times,
        Expression perform
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        var local = ctx.bytecodeUnsafe().allocateLocal(TypeKind.INT);
        ctx.pushValue(times)
                .typecheck(Double.class)
                .unboxNumber()
                .d2i()
                .istore(local);

        var loopCheck = ctx.bytecodeUnsafe().newLabel();

        var loopStart = ctx.bytecodeUnsafe().newLabel();
        var loopExit = ctx.bytecodeUnsafe().newLabel();

        ctx.bytecodeUnsafe(
                cb -> cb.labelBinding(loopCheck)
                        .iload(local)
                        .loadConstant(0)
                        .if_icmpgt(loopStart)
                        .goto_(loopExit)
                        .labelBinding(loopStart))
                .pushValue(perform)
                .bytecodeUnsafe(cb -> cb.iload(local)
                        .loadConstant(1)
                        .isub()
                        .istore(local)
                        .goto_(loopCheck)
                        .labelBinding(loopExit));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
