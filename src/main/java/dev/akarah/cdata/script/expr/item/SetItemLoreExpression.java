package dev.akarah.cdata.script.expr.item;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.item.ItemStack;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

public record SetItemLoreExpression(
        Expression itemValue,
        Expression loreList
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(itemValue)
                .typecheck(ItemStack.class)
                .pushValue(loreList)
                .typecheck(ArrayList.class)
                .bytecode(cb -> cb.invokestatic(
                        CodegenUtil.ofClass(ItemUtil.class),
                        "setItemLore",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(CodegenUtil.ofClass(ItemStack.class), CodegenUtil.ofClass(ArrayList.class))
                        )
                ));
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("item", Type.itemStack()),
                Pair.of("lore", Type.list())
        );
    }
}
