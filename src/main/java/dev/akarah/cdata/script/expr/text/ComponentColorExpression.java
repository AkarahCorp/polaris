package dev.akarah.cdata.script.expr.text;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record ComponentColorExpression(
        Expression component,
        Expression color
) implements Expression {

    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.component)
                .pushValue(this.color)
                .invokeStatic(
                        CodegenUtil.ofClass(TextUtil.class),
                        "withColor",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(MutableComponent.class),
                                List.of(CodegenUtil.ofClass(MutableComponent.class), CodegenUtil.ofClass(String.class))
                        )
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.text();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("component", Type.text()),
                Pair.of("color", Type.string())
        );
    }
}
