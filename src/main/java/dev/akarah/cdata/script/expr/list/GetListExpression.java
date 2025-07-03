package dev.akarah.cdata.script.expr.list;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

public record GetListExpression(
        Expression listValue,
        Expression index
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(listValue)
                .typecheck(ArrayList.class)
                .pushValue(index)
                .typecheck(Double.class)
                .unboxNumber()
                .bytecode(CodeBuilder::d2i)
                .bytecode(cb -> cb.invokevirtual(
                        JIT.ofClass(ArrayList.class),
                        "get",
                        MethodTypeDesc.of(
                                JIT.ofClass(Object.class),
                                List.of(JIT.ofInt())
                        )
                ));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.any();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("list", Type.list()),
                Pair.of("index", Type.number())
        );
    }
}
