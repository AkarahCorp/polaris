package dev.akarah.cdata.script.expr.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.animal.Cod;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record ComponentLiteralExpression(String value) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb -> cb.loadConstant(this.value).invokestatic(
                CodegenUtil.ofClass(Component.class),
                "literal",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(MutableComponent.class),
                        List.of(CodegenUtil.ofClass(String.class))
                ),
                true
        ));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.text();
    }
}
