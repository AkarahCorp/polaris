package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RNumber;

import java.lang.classfile.TypeKind;

public record RepeatTimesAction(
        Expression times,
        Expression perform
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        var local = ctx.bytecodeUnsafe().allocateLocal(TypeKind.INT);
        ctx.pushValue(times)
                .typecheck(RNumber.class)
                .unboxNumber()
                .d2i()
                .istore(local);

        var loopCheck = ctx.bytecodeUnsafe().newLabel();

        var loopStart = ctx.bytecodeUnsafe().newLabel();
        var loopExit = ctx.bytecodeUnsafe().newLabel();

        ctx
                .pushFrame(loopCheck, loopExit)
                .bytecodeUnsafe(
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
