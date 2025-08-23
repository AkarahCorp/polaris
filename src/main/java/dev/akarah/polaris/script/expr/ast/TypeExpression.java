package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.StructType;
import dev.akarah.polaris.script.type.Type;
import net.minecraft.resources.ResourceLocation;

public record TypeExpression(
        ResourceLocation name,
        StructType alias
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {

    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return this.alias;
    }
}
