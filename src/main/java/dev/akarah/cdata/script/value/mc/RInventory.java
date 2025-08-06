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

    private RInventory(Container inner, RText name) {
        this.inner = inner;
        this.name = name;
    }

    public static RInventory of(Container value, RText name) {
        return new RInventory(value, name);
    }

    @Override
    public Container javaValue() {
        return this.inner;
    }

    @MethodTypeHint(signature = "(this: inventory, slot: number) -> item", documentation = "Returns the item provided in the given slot of this inventory.")
    public static RItem get_slot(RInventory $this, RNumber slot) {
        try {
            return RItem.of($this.inner.getItem(slot.intValue()));
        } catch (Exception e) {
            return RItem.of(ItemStack.EMPTY);
        }
    }

    @MethodTypeHint(signature = "(this: inventory, slot: item) -> boolean", documentation = "")
    public static RBoolean has(RInventory $this, RItem item) {
        int counted = 0;
        for(int i = 0; i < $this.inner.getContainerSize(); i++) {
            if($this.inner.getItem(i).is(Items.AIR)) {
                continue;
            }
            if(ItemStack.isSameItemSameComponents($this.inner.getItem(i), item.javaValue())) {
                counted += $this.inner.getItem(i).getCount();
            }
        }
        return RBoolean.of(counted >= item.javaValue().getCount());
    }

    @MethodTypeHint(signature = "(this: inventory, slot: item) -> void", documentation = "")
    public static void remove(RInventory $this, RItem item) {
        int counted = item.javaValue().getCount();
        for(int i = 0; i < $this.inner.getContainerSize(); i++) {
            var subitem = $this.inner.getItem(i);
            if(subitem.is(Items.AIR)) {
                continue;
            }
            if(ItemStack.isSameItemSameComponents(subitem, item.javaValue())) {
                counted -= subitem.getCount();
                $this.inner.setItem(i, subitem.copyWithCount(Math.max(counted * -1, 0)));
            }
            if(counted <= 0) {
                return;
            }
        }
    }

    @MethodTypeHint(signature = "(this: inventory, slot: number, item: item) -> void", documentation = "Sets the item provided in the given slot of this inventory.")
    public static void set_slot(RInventory $this, RNumber slot, RItem item) {
        $this.inner.setItem(slot.intValue(), item.javaValue());
    }

    @MethodTypeHint(signature = "(this: inventory, item: item) -> boolean", documentation = "Adds a new item to the inventory if there is room for it.")
    public static RBoolean has_room_for(RInventory $this, RItem item) {
        for(int i = 0; i < $this.inner.getContainerSize(); i++) {
            if($this.inner.getItem(i).is(Items.AIR)) {
                return RBoolean.of(true);
            }
            if(ItemStack.isSameItemSameComponents($this.inner.getItem(i), item.javaValue())) {
                var sumCounts = $this.inner.getItem(i).getCount() + item.javaValue().getCount();

                var maxCount = item.javaValue().get(DataComponents.MAX_STACK_SIZE);
                if(maxCount == null) {
                    maxCount = 1;
                }

                if(sumCounts <= maxCount) {
                    return RBoolean.of(true);
                }
            }
        }
        return RBoolean.of(false);
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

    @MethodTypeHint(signature = "(this: inventory) -> text", documentation = "Gets the name of a custom inventory.")
    public static RText name(RInventory $this) {
        return $this.name;
    }

    @MethodTypeHint(signature = "(this: inventory) -> list[item]", documentation = "Gets all items of this inventory in a list.")
    public static RList items(RInventory $this) {
        var list = RList.create();
        for(var item : $this.inner) {
            if(item == null) {
                list.javaValue().add(RItem.of(ItemStack.EMPTY));
            }
            list.javaValue().add(RItem.of(item));
        }
        return list;
    }

    @MethodTypeHint(signature = "(this: inventory, items: list[item]) -> void", documentation = "Sets all items of this inventory to the list provided.")
    public static void set_items(RInventory $this, RList list) {
        $this.inner.clearContent();
        int slot = 0;
        for(var item : list.javaValue()) {
            if(item.javaValue() instanceof ItemStack itemStack) {
                $this.inner.setItem(slot, itemStack);
            }
            slot++;
        }
    }
}
