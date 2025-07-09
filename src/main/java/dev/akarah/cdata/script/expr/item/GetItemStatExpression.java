package dev.akarah.cdata.script.expr.item;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
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
                .invokeStatic(
                        CodegenUtil.ofClass(ItemUtil.class),
                        "getItemStat",
                        MethodTypeDesc.of(
                                CodegenUtil.ofDouble(),
                                List.of(CodegenUtil.ofClass(ItemStack.class), CodegenUtil.ofClass(String.class))
                        )
                )
                .boxNumber();
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("item", Type.itemStack())
                .required("stat", Type.string())
                .returns(Type.number())
                .build();
    }
}
