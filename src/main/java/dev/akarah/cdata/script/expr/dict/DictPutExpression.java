package dev.akarah.cdata.script.expr.dict;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.HashMap;
import java.util.List;

public record DictPutExpression(
        Expression dict,
        Expression key,
        Expression value
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(dict)
                .typecheck(HashMap.class)
                .pushValue(key)
                .pushValue(value)
                .invokeVirtual(
                        CodegenUtil.ofClass(HashMap.class),
                        "put",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Object.class),
                                List.of(CodegenUtil.ofClass(Object.class), CodegenUtil.ofClass(Object.class))
                        )
                )
                .pop();

    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.any();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("map", Type.dict(Type.any(), Type.any())),
                Pair.of("key", Type.any()),
                Pair.of("value", Type.any())
        );
    }
}
