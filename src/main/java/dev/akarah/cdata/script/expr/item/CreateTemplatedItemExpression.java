package dev.akarah.cdata.script.expr.item;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record CreateTemplatedItemExpression(
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

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.itemStack();
    }

    public static List<Pair<String, Type<?>>> fields() {
        return List.of(
                Pair.of("id", Type.identifier()),
                Pair.of("template", Type.identifier())
        );
    }
}
