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
        ctx.pushValue(times).typecheck(Double.class);
        var local = ctx.bytecode().allocateLocal(TypeKind.INT);
        ctx.unboxNumber().bytecode(CodeBuilder::d2i).bytecode(cb -> cb.istore(local));

        var loopCheck = ctx.bytecode().newLabel();

        var loopStart = ctx.bytecode().newLabel();
        var loopExit = ctx.bytecode().newLabel();

        ctx.bytecode(
                cb -> cb.labelBinding(loopCheck)
                        .iload(local)
                        .loadConstant(0)
                        .if_icmpgt(loopStart)
                        .goto_(loopExit)
                        .labelBinding(loopStart))
                .pushValue(perform)
                .bytecode(cb -> cb.iload(1)
                        .loadConstant(1)
                        .isub()
                        .istore(1)
                        .goto_(loopCheck)
                        .labelBinding(loopExit));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }

    @Override
    public int localsRequiredForCompile() {
        return this.perform.localsRequiredForCompile() + 1;
    }
}
