package dev.akarah.cdata.script.expr.flow;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public record SchemaExpression(
        List<Pair<String, Type<?>>> parameters,
        Type<?> returnType,
        AllOfAction body
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        this.body().compile(ctx);
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return this.returnType;
    }
}
