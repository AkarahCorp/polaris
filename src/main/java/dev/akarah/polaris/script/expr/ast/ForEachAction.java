package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.exception.ParsingException;
import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.ListType;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.RList;
import dev.akarah.polaris.script.value.RuntimeValue;

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

        var listValueType = ctx.getTypeOf(list).flatten();
        Type<?> listSubType;
        if(listValueType instanceof ListType(Type<?> subtype))  {
            listSubType = subtype;
        } else {
            throw new ParsingException("Expected a list in list position", this.span());
        }
        ctx.pushValue(list)
                .typecheck(RList.class)
                .invokeStatic(
                        CodegenUtil.ofClass(RList.class),
                        "copy",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(RList.class),
                                List.of(CodegenUtil.ofClass(RList.class))
                        )
                )
                .invokeVirtual(
                        CodegenUtil.ofClass(RuntimeValue.class),
                        "javaValue",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Object.class),
                                List.of()
                        )
                )
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
        var loopExitLabel = ctx.bytecodeUnsafe().newLabel();
        ctx
                .bytecodeUnsafe(cb -> cb.labelBinding(loopJumpLabel))
                .aload(local)
                .typecheck(Iterator.class)
                .invokeInterface(
                        CodegenUtil.ofClass(Iterator.class),
                        "hasNext",
                        MethodTypeDesc.of(CodegenUtil.ofBoolean(), List.of())
                )
                .ifThen(
                        () -> ctx.aload(local)
                                .typecheck(Iterator.class)
                                .invokeInterface(
                                        CodegenUtil.ofClass(Iterator.class),
                                        "next",
                                        MethodTypeDesc.of(
                                                CodegenUtil.ofClass(Object.class),
                                                List.of()
                                        )
                                )
                                .typecheck(listSubType.typeClass())
                                .pushFrame(loopJumpLabel, loopExitLabel)
                                .storeLocalShadowing(this.variableName(), listSubType)
                                .pushValue(this.block)
                                .popFrame()
                                .bytecodeUnsafe(cb -> cb.goto_(loopJumpLabel))
                )
                .bytecodeUnsafe(cb -> cb.labelBinding(loopExitLabel));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
