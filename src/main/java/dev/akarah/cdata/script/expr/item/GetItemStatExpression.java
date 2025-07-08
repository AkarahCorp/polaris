package dev.akarah.cdata.script.expr.item;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.item.ItemStack;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record GetItemStatExpression(
        Expression itemValue,
        Expression stat
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(itemValue)
                .typecheck(ItemStack.class)
                .pushValue(stat)
                .typecheck(String.class)
                .bytecode(cb -> cb.invokestatic(
                        CodegenUtil.ofClass(ItemUtil.class),
                        "getItemStat",
                        MethodTypeDesc.of(
                                CodegenUtil.ofDouble(),
                                List.of(CodegenUtil.ofClass(ItemStack.class), CodegenUtil.ofClass(String.class))
                        )
                ))
                .boxNumber();
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.number();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("item", Type.itemStack()),
                Pair.of("stat", Type.string())
        );
    }
}
