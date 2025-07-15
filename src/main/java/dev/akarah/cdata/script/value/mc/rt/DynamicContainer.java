package dev.akarah.cdata.script.value.mc.rt;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class DynamicContainer extends SimpleContainer {
    public boolean cancelClicks = false;

    public DynamicContainer(int slots) {
        super(slots);
    }

    public NonNullList<ItemStack> items() {
        return this.items;
    }

    public boolean cancelClicks() {
        return cancelClicks;
    }
}
