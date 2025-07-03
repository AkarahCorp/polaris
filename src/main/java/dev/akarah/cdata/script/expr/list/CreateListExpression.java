package dev.akarah.cdata.script.expr.list;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

public record CreateListExpression() implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb -> cb.invokestatic(
                JIT.ofClass(Lists.class),
                "newArrayList",
                MethodTypeDesc.of(
                        JIT.ofClass(ArrayList.class),
                        List.of()
                )
        ));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.list();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of();
    }
}
