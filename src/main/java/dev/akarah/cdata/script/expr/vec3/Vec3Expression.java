package dev.akarah.cdata.script.expr.vec3;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.phys.Vec3;

import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Optional;

public record Vec3Expression(
        Expression x,
        Expression y,
        Expression z
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .bytecode(cb -> cb.new_(JIT.ofClass(Vec3.class)).dup())
                .pushValue(this.x)
                .pushValue(this.y)
                .pushValue(this.z)
                .bytecode(cb -> cb.invokespecial(
                        JIT.ofClass(Vec3.class),
                        "<init>",
                        MethodTypeDesc.of(
                                JIT.ofVoid(),
                                List.of(JIT.ofDouble(), JIT.ofDouble(), JIT.ofDouble()
                        )
                )));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.vec3();
    }
}
