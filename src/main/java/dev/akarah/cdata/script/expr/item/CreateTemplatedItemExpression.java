package dev.akarah.cdata.script.expr.item;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record CreateTemplatedItemExpression(
        Expression itemId,
        Expression templateId
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(itemId)
                .typecheck(ResourceLocation.class)
                .pushValue(templateId)
                .typecheck(ResourceLocation.class)
                .invokeStatic(
                        CodegenUtil.ofClass(ItemUtil.class),
                        "renderItemTemplate",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(ItemStack.class),
                                List.of(
                                        CodegenUtil.ofClass(ResourceLocation.class),
                                        CodegenUtil.ofClass(ResourceLocation.class)
                                )
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("item_id", Type.identifier())
                .required("template_id", Type.identifier())
                .returns(Type.itemStack())
                .build();
    }
}
