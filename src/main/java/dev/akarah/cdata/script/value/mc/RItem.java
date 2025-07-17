package dev.akarah.cdata.script.value.mc;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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

    @MethodTypeHint("(item: item, key: string) -> nullable[any]")
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

    @MethodTypeHint("(item: item, key: string, value: any) -> void")
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
