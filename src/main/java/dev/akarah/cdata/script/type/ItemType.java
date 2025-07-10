package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.constant.ClassDesc;

public record ItemType() implements Type<ItemStack> {
    @Override
    public String typeName() {
        return "item";
    }

    @Override
    public Class<ItemStack> typeClass() {
        return ItemStack.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(ItemStack.class);
    }
}
