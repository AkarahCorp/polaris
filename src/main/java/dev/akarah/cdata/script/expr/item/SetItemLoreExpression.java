package dev.akarah.cdata.script.expr.item;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.item.ItemStack;

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
                .invokeStatic(
                        CodegenUtil.ofClass(ItemUtil.class),
                        "setItemLore",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(CodegenUtil.ofClass(ItemStack.class), CodegenUtil.ofClass(ArrayList.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("item", Type.itemStack())
                .required("lore", Type.list(Type.text()))
                .build();
    }
}
