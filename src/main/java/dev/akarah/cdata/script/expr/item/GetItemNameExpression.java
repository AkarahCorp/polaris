package dev.akarah.cdata.script.expr.item;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.world.item.ItemStack;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record GetItemNameExpression(
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

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("item", Type.itemStack())
                .returns(Type.string())
                .build();
    }
}
