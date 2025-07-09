package dev.akarah.cdata.script.expr.list;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

public record AddListExpression(
        Expression listValue,
        Expression valueToPush
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(listValue)
                .typecheck(ArrayList.class)
                .pushValue(valueToPush)
                .invokeVirtual(
                        CodegenUtil.ofClass(ArrayList.class),
                        "add",
                        MethodTypeDesc.of(
                                CodegenUtil.ofBoolean(),
                                List.of(CodegenUtil.ofClass(Object.class))
                        )
                )
                .pop();
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("list", Type.list(Type.any())),
                Pair.of("value", Type.any())
        );
    }
}
