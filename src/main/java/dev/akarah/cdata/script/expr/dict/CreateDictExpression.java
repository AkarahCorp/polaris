package dev.akarah.cdata.script.expr.dict;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record CreateDictExpression() implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb -> cb.invokestatic(
                JIT.ofClass(Maps.class),
                "newHashMap",
                MethodTypeDesc.of(
                        JIT.ofClass(HashMap.class),
                        List.of()
                )
        ));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.dict();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of();
    }
}
