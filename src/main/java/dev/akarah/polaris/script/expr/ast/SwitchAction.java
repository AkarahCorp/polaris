package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RBoolean;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Opcode;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;

public record SwitchAction(
        Expression value,
        List<Case> cases
) implements Expression {
    public record Case(Expression comparison, Expression block, Optional<Expression> where) {

    }
    @Override
    public void compile(CodegenContext ctx) {
        var exitLabel = ctx.bytecodeUnsafe().newLabel();

        for(var case_ : cases) {
            ctx
                    .pushValue(this.value)
                    .pushValue(case_.comparison())
                    .invokeVirtual(
                            CodegenUtil.ofClass(Object.class),
                            "equals",
                            MethodTypeDesc.of(
                                    CodegenUtil.ofBoolean(),
                                    CodegenUtil.ofClass(Object.class)
                            )
                    );
            case_.where().ifPresent(where -> ctx.pushValue(where)
                    .invokeVirtual(
                            CodegenUtil.ofClass(RBoolean.class),
                            "asInt",
                            MethodTypeDesc.of(
                                    CodegenUtil.ofInt(),
                                    List.of()
                            )
                    ).bytecodeUnsafe(CodeBuilder::iand)
            );
            ctx
                    .pushFrame(exitLabel, ctx.getFrame().breakLabel())
                    .ifThen(
                            Opcode.IFNE,
                            () -> ctx.pushValue(case_.block()).bytecodeUnsafe(cb -> cb.goto_(exitLabel))
                    )
                    .popFrame();
        }
        ctx.bytecodeUnsafe(cb -> cb.labelBinding(exitLabel));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
