package dev.akarah.cdata.script.expr.dict;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.MethodTypeDesc;
import java.util.HashMap;
import java.util.List;

public record CreateDictExpression() implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.invokeStatic(
                CodegenUtil.ofClass(Maps.class),
                "newHashMap",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(HashMap.class),
                        List.of()
                )
        );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.dict();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of();
    }
}
