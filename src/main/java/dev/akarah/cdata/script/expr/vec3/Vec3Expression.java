package dev.akarah.cdata.script.expr.vec3;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record Vec3Expression(
        Expression x,
        Expression y,
        Expression z
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.x)
                .typecheck(Double.class)
                .pushValue(this.y)
                .typecheck(Double.class)
                .pushValue(this.z)
                .typecheck(Double.class)
                .invokeStatic(
                        CodegenUtil.ofClass(Vec3Util.class),
                        "create",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Vec3.class),
                                List.of(CodegenUtil.ofClass(Double.class), CodegenUtil.ofClass(Double.class), CodegenUtil.ofClass(Double.class))
                        )
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.vec3();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("x", Type.number()),
                Pair.of("y", Type.number()),
                Pair.of("z", Type.number())
        );
    }
}
