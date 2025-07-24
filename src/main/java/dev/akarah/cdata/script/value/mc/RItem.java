package dev.akarah.cdata.script.value.mc;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.*;
import dev.akarah.cdata.script.value.event.RItemEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.Objects;
import java.util.Optional;

public class RItem extends RuntimeValue {
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

    @MethodTypeHint(signature = "(item: item) -> identifier", documentation = "Gets the ID of the item stack.")
    public static RIdentifier id(RItem item) {
        return RIdentifier.of(
                CustomItem.itemIdOf(item.javaValue())
                        .orElse(item.javaValue().getItem().builtInRegistryHolder().key().location())
        );
    }

    @MethodTypeHint(signature = "(item: item, name: text) -> void", documentation = "Sets the name of the item stack.")
    public static void set_name(RItem item, RText name) {
        item.javaValue().set(DataComponents.ITEM_NAME, name.javaValue());
        item.javaValue().set(DataComponents.CUSTOM_NAME, name.javaValue());;
    }

    @MethodTypeHint(signature = "(item: item) -> string", documentation = "Gets the name of the base item.")
    public static RString name(RItem $this) {
        return RString.of(CustomItem.itemOf($this.javaValue()).flatMap(CustomItem::name)
                .orElse($this.javaValue().getItemName().getString()));
    }

    @MethodTypeHint(signature = "(item: item, stat: string) -> number", documentation = "Gets the value of the stat provided associated with this item.")
    public static RNumber stat(RItem $this, RString stat) {
        return RNumber.of(CustomItem.itemOf($this.javaValue())
                .flatMap(CustomItem::stats)
                .map(x -> x.get(stat.javaValue()))
                .orElse(-1.0));
    }

    @MethodTypeHint(signature = "(item: item, key: string) -> nullable[any]", documentation = "Gets a custom item tag from the item, based on the key provided.")
    public static RNullable tag(RItem $this, RString keyTag) {
        return RNullable.of(
                Optional.<RuntimeValue>empty()
                        .or(() -> CustomItem.itemOf($this.javaValue()).flatMap(CustomItem::customData)
                                .flatMap(x -> Optional.ofNullable(x.get(keyTag.javaValue()))))
                        .or(() -> Optional.ofNullable($this.javaValue().get(DataComponents.CUSTOM_DATA))
                                .flatMap(x -> Optional.ofNullable(x.getUnsafe().get(keyTag.javaValue())))
                                .flatMap(x -> RuntimeValue.CODEC.decode(NbtOps.INSTANCE, x).result().map(Pair::getFirst)))
                        .orElse(null)
        );
    }

    @MethodTypeHint(signature = "(item: item, key: string, value: any) -> void", documentation = "Sets an item tag on the item, held with the key provided.")
    public static void set_tag(RItem $this, RString keyTag, RuntimeValue keyValue) {
        if(!$this.javaValue().has(DataComponents.CUSTOM_DATA)) {
            $this.javaValue().set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
        }

        $this.javaValue().set(
                DataComponents.CUSTOM_DATA,
                Objects.requireNonNull($this.javaValue().get(DataComponents.CUSTOM_DATA)).update(tag -> tag.put(
                        keyTag.javaValue(),
                        RuntimeValue.CODEC.encodeStart(NbtOps.INSTANCE, keyValue).result().orElse(DoubleTag.valueOf(0.0))
                ))
        );
    }

    @MethodTypeHint(signature = "(item: item) -> list[text]", documentation = "Gets the current lore of the item stack.")
    public static RList lore(RItem $this) {
        var list = RList.create();
        var lore = $this.javaValue().get(DataComponents.LORE);
        if(lore != null) {
            for(var line : lore.styledLines()) {
                RList.add(list, RText.of(line));
            }
        }
        return list;
    }

    @MethodTypeHint(signature = "(item: item, lore: list[text]) -> void", documentation = "Sets the lore of the item stack.")
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

    @MethodTypeHint(signature = "(item: item, amount: number) -> void", documentation = "Sets the amount of items in the item stack.")
    public static void set_amount(RItem $this, RNumber amount) {
        $this.javaValue().setCount(amount.intValue());
    }

    @MethodTypeHint(signature = "(item: item) -> number", documentation = "Returns the amount of items in the item stack.")
    public static RNumber amount(RItem $this) {
        return RNumber.of($this.javaValue().getCount());
    }
}
