package dev.akarah.cdata.script.value.mc;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.*;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RInventory extends RuntimeValue {
    Container inner;
    RText name = RText.of(Component.literal("Menu"));

    private RInventory(Container inner) {
        this.inner = inner;
    }

    public static RInventory of(Container value) {
        return new RInventory(value);
    }

    @Override
    public Container javaValue() {
        return this.inner;
    }

    @MethodTypeHint(signature = "(this: inventory, slot: number, item: item) -> void", documentation = "Returns the item provided in the given slot of this inventory.")
    public static void set_slot(RInventory $this, RNumber slot, RItem item) {
        $this.inner.setItem(slot.intValue(), item.javaValue());
    }

    @MethodTypeHint(signature = "(this: inventory, item: item) -> void", documentation = "Adds a new item to the inventory if there is room for it.")
    public static void add_item(RInventory $this, RItem item) {
        for(int i = 0; i < $this.inner.getContainerSize(); i++) {
            if($this.inner.getItem(i).is(Items.AIR)) {
                $this.inner.setItem(i, item.javaValue());
                return;
            }
            if(ItemStack.isSameItemSameComponents($this.inner.getItem(i), item.javaValue())) {
                var sumCounts = $this.inner.getItem(i).getCount() + item.javaValue().getCount();

                var maxCount = item.javaValue().get(DataComponents.MAX_STACK_SIZE);
                if(maxCount == null) {
                    maxCount = 1;
                }

                if(sumCounts <= maxCount) {
                    $this.inner.setItem(i, item.javaValue().copyWithCount(sumCounts));
                    return;
                }
                continue;
            }

        }
    }

    @MethodTypeHint(signature = "(this: inventory) -> void", documentation = "If this is a custom inventory, this will no longer be able to be manipulated by players.")
    public static void cancel_clicks(RInventory $this) {
        if($this.inner instanceof DynamicContainer dynamicContainer) {
            dynamicContainer.cancelClicks = true;
        }
    }

    @MethodTypeHint(signature = "(this: inventory, name: text) -> void", documentation = "Sets the name of a custom inventory.")
    public static void set_name(RInventory $this, RText name) {
        $this.name = name;
    }
}
