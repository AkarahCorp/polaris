package dev.akarah.cdata.script.value.mc;

import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public class RItem extends RuntimeValue<ItemStack> {
    private final ItemStack inner;

    private RItem(ItemStack inner) {
        this.inner = inner;
    }

    public static RItem of(ItemStack value) {
        return new RItem(value);
    }

    @Override
    public ItemStack javaValue() {
        return this.inner;
    }

    @MethodTypeHint("(item: item, name: text) -> void")
    public static void set_name(RItem item, RText name) {
        item.javaValue().set(DataComponents.ITEM_NAME, name.javaValue());
    }

    @MethodTypeHint("(item: item) -> string")
    public static RString name(RItem $this) {
        return RString.of(CustomItem.itemOf($this.javaValue()).flatMap(CustomItem::name)
                .orElse($this.javaValue().getItemName().getString()));
    }

    @MethodTypeHint("(item: item, stat: string) -> number")
    public static RNumber stat(RItem $this, RString stat) {
        return RNumber.of(CustomItem.itemOf($this.javaValue())
                .flatMap(CustomItem::stats)
                .map(x -> x.get(stat.javaValue()))
                .orElse(-1.0));
    }

    @MethodTypeHint("(item: item, lore: list[text]) -> void")
    public static void set_lore(RItem $this, RList name) {
        $this.javaValue().set(
                DataComponents.LORE,
                new ItemLore(
                        name.javaValue().stream()
                                .filter(x -> x instanceof RText)
                                .map(x -> ((RText) x).javaValue())
                                .toList()
                )
        );
    }
}
