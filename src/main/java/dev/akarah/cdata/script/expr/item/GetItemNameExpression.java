package dev.akarah.cdata.script.expr.item;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record GetItemNameExpression(
        Expression itemValue
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(itemValue)
                .typecheck(ItemStack.class)
                .invokeStatic(
                        CodegenUtil.ofClass(ItemUtil.class),
                        "getItemName",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(String.class),
                                List.of(CodegenUtil.ofClass(ItemStack.class))
                        )
                );
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.string();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("item", Type.itemStack())
        );
    }
}
