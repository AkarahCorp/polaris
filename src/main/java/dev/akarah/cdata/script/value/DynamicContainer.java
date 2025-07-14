package dev.akarah.cdata.script.value;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DynamicContainer extends SimpleContainer {
    boolean cancelClicks = false;

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
