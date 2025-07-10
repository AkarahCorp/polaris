package dev.akarah.cdata.script.expr.world;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

public record GetWorldEntitiesExpression(
        Expression world
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.world)
                .typecheck(Level.class)
                .invokeStatic(
                        CodegenUtil.ofClass(WorldUtil.class),
                        "entities",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(ArrayList.class),
                                List.of(CodegenUtil.ofClass(Level.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("world", Type.world())
                .returns(Type.list(Type.entity()))
                .build();
    }
}
