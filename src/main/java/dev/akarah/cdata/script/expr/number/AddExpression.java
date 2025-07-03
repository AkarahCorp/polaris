package dev.akarah.cdata.script.expr.number;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.phys.Vec3;

import java.lang.classfile.CodeBuilder;

public record AddExpression(
        Expression lhs,
        Expression rhs
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(lhs)
                .typecheck(Double.class)
                .unboxNumber()
                .pushValue(rhs)
                .typecheck(Double.class)
                .unboxNumber()
                .bytecode(CodeBuilder::dadd)
                .boxNumber();
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.number();
    }
}
