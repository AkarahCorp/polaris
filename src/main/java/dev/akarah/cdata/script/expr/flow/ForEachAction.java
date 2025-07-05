package dev.akarah.cdata.script.expr.flow;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.TypeKind;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record ForEachAction(
        Expression list,
        String variableName,
        Expression block
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        var local = ctx.bytecode().allocateLocal(TypeKind.REFERENCE);
        ctx.pushValue(list)
                .typecheck(ArrayList.class)
                .bytecode(cb -> cb.invokevirtual(
                    CodegenUtil.ofClass(ArrayList.class),
                    "iterator",
                    MethodTypeDesc.of(
                            CodegenUtil.ofClass(Iterator.class),
                            List.of()
                    )
                ))
                .typecheck(Iterator.class)
                .bytecode(cb -> cb.astore(local));

        var loopJumpLabel = ctx.bytecode().newLabel();
        ctx.bytecode(cb -> cb.labelBinding(loopJumpLabel))
                .bytecode(cb -> cb.aload(local))
                .typecheck(Iterator.class)
                .bytecode(cb -> cb.invokeinterface(
                        CodegenUtil.ofClass(Iterator.class),
                        "hasNext",
                        MethodTypeDesc.of(CodegenUtil.ofBoolean(), List.of())
                ))
                .ifThen(
                        () -> ctx.bytecode(cb -> cb.aload(local))
                                .bytecode(cb -> cb.invokeinterface(
                                        CodegenUtil.ofClass(Iterator.class),
                                        "next",
                                        MethodTypeDesc.of(
                                                CodegenUtil.ofClass(Object.class),
                                                List.of()
                                        )
                                ))
                                .storeLocal(this.variableName(), Type.any())
                                .pushValue(this.block)
                                .bytecode(cb -> cb.goto_(loopJumpLabel))
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }

    @Override
    public int localsRequiredForCompile() {
        return this.block.localsRequiredForCompile();
    }
}
