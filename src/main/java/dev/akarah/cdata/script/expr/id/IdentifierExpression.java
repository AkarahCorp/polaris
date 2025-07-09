package dev.akarah.cdata.script.expr.id;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

public record IdentifierExpression(
        Expression namespace,
        Expression path
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(namespace)
                .typecheck(String.class)
                .pushValue(path)
                .typecheck(String.class)
                .invokeStatic(
                        CodegenUtil.ofClass(ResourceLocationUtil.class),
                        "create",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(ResourceLocation.class),
                                List.of(CodegenUtil.ofClass(String.class), CodegenUtil.ofClass(String.class))
                        )
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.identifier();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("namespace", Type.string()),
                Pair.of("path", Type.string())
        );
    }
}
