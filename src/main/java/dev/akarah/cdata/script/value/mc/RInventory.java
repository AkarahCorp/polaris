package dev.akarah.cdata.script.value.mc;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.*;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;

public class RInventory extends RuntimeValue<Container> {
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

    @MethodTypeHint("(this: inventory, slot: number, item: item) -> void")
    public static void set_slot(RInventory $this, RNumber slot, RItem item) {
        $this.inner.setItem(slot.intValue(), item.javaValue());
    }

    @MethodTypeHint("(this: inventory, item: item) -> void")
    public static void add_item(RInventory $this, RItem item) {
        for(int i = 0; i < $this.inner.getContainerSize(); i++) {
            if($this.inner.getItem(i).is(Items.AIR)) {
                $this.inner.setItem(i, item.javaValue());
                return;
            }
        }
    }

    @MethodTypeHint("(this: inventory) -> void")
    public static void cancel_clicks(RInventory $this) {
        if($this.inner instanceof DynamicContainer dynamicContainer) {
            dynamicContainer.cancelClicks = true;
        }
    }

    @MethodTypeHint("(this: inventory, name: text) -> void")
    public static void set_name(RInventory $this, RText name) {
        $this.name = name;
    }
}
