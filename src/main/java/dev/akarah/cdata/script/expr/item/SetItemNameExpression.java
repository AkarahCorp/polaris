package dev.akarah.cdata.script.expr.item;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record SetItemNameExpression(
        Expression itemValue,
        Expression itemName
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(itemValue)
                .typecheck(ItemStack.class)
                .pushValue(itemName)
                .typecheck(Component.class)
                .invokeStatic(
                        CodegenUtil.ofClass(ItemUtil.class),
                        "setItemName",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(CodegenUtil.ofClass(ItemStack.class), CodegenUtil.ofClass(Component.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("item", Type.itemStack())
                .required("name", Type.text())
                .build();
    }
}
