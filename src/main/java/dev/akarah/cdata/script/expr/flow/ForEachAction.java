package dev.akarah.cdata.script.expr.flow;

import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.ListType;
import dev.akarah.cdata.script.type.SpannedType;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.TypeKind;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record ForEachAction(
        Expression list,
        String variableName,
        Expression block,
        SpanData span
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        var local = ctx.bytecodeUnsafe().allocateLocal(TypeKind.REFERENCE);

        var listValueType = ctx.getTypeOf(list).despan();
        Type<?> listSubType;
        if(listValueType instanceof ListType(Type<?> subtype))  {
            listSubType = subtype;
        } else {
            throw new ParsingException("Expected a list in list position", this.span());
        }
        ctx.pushValue(list)
                .typecheck(ArrayList.class)
                .invokeVirtual(
                    CodegenUtil.ofClass(ArrayList.class),
                    "iterator",
                    MethodTypeDesc.of(
                            CodegenUtil.ofClass(Iterator.class),
                            List.of()
                    )
                )
                .typecheck(Iterator.class)
                .astore(local);

        var loopJumpLabel = ctx.bytecodeUnsafe().newLabel();
        ctx.bytecodeUnsafe(cb -> cb.labelBinding(loopJumpLabel))
                .aload(local)
                .typecheck(Iterator.class)
                .invokeInterface(
                        CodegenUtil.ofClass(Iterator.class),
                        "hasNext",
                        MethodTypeDesc.of(CodegenUtil.ofBoolean(), List.of())
                )
                .ifThen(
                        () -> ctx.aload(local)
                                .invokeInterface(
                                        CodegenUtil.ofClass(Iterator.class),
                                        "next",
                                        MethodTypeDesc.of(
                                                CodegenUtil.ofClass(Object.class),
                                                List.of()
                                        )
                                )
                                .storeLocal(this.variableName(), listSubType)
                                .pushValue(this.block)
                                .bytecodeUnsafe(cb -> cb.goto_(loopJumpLabel))
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
