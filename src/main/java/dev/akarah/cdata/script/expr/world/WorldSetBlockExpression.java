package dev.akarah.cdata.script.expr.world;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Map;

public record WorldSetBlockExpression(
        Expression world,
        Expression position,
        Expression blockId,
        Expression blockStateMap
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.world)
                .pushValue(this.position)
                .pushValue(this.blockId)
                .pushValue(this.blockStateMap)
                .invokeStatic(
                        CodegenUtil.ofClass(WorldUtil.class),
                        "setBlock",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(
                                        CodegenUtil.ofClass(Level.class),
                                        CodegenUtil.ofClass(Vec3.class),
                                        CodegenUtil.ofClass(ResourceLocation.class),
                                        CodegenUtil.ofClass(Map.class)
                                )
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("world_id", Type.world())
                .required("pos", Type.vector())
                .required("block_id", Type.identifier())
                .optional("state", Type.dict(Type.string(), Type.string()))
                .returns(Type.world())
                .build();
    }
}
