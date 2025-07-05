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
                .bytecode(cb -> cb.new_(CodegenUtil.ofClass(Vec3.class)).dup())
                .pushValue(this.x)
                .typecheck(Double.class)
                .unboxNumber()
                .pushValue(this.y)
                .typecheck(Double.class)
                .unboxNumber()
                .pushValue(this.z)
                .typecheck(Double.class)
                .unboxNumber()
                .bytecode(cb -> cb.invokespecial(
                        CodegenUtil.ofClass(Vec3.class),
                        "<init>",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(CodegenUtil.ofDouble(), CodegenUtil.ofDouble(), CodegenUtil.ofDouble()
                        )
                )));
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
