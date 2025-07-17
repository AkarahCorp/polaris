package dev.akarah.cdata.script.value;

import dev.akarah.cdata.db.Database;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.mc.RVector;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainer;
import dev.akarah.cdata.script.value.mc.RIdentifier;
import dev.akarah.cdata.script.value.mc.RInventory;
import dev.akarah.cdata.script.value.mc.RItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GlobalNamespace {
    @MethodTypeHint("(min: number, max: number) -> list[number]")
    public static RList range(RNumber min, RNumber max) {
        var list = RList.create();

        var newMin = min.javaValue();
        var newMax = max.javaValue();
        var minIsLower = newMin < newMax;

        double idx = minIsLower ? newMin : newMax;
        while(idx <= (minIsLower ? newMax : newMin)) {
            RList.add(list, RNumber.of(idx));
            idx += 1;
        }
        return list;
    }

    @MethodTypeHint("(x: number, y: number, z: number) -> vector")
    public static RVector vec(RNumber x, RNumber y, RNumber z) {
        return RVector.of(new Vec3(x.doubleValue(), y.doubleValue(), z.doubleValue()));
    }

    @MethodTypeHint("(v: any) -> text")
    public static RText text(RuntimeValue<?> runtimeValue) {
        return RText.of(Component.literal(runtimeValue.toString()).withStyle(s -> s.withItalic(false)));
    }

    public static RText textLiteralInternal(Object runtimeValue) {
        return RText.of(Component.literal(runtimeValue.toString()).withStyle(s -> s.withItalic(false)));
    }

    @MethodTypeHint("(namespace: string, path: string) -> identifier")
    public static RIdentifier id(RString namespace, RString path) {
        return RIdentifier.of(ResourceLocation.fromNamespaceAndPath(namespace.javaValue(), path.javaValue()));
    }

    @MethodTypeHint("(item_id: identifier) -> item")
    public static RItem item__create(RIdentifier id) {
        return RItem.of(CustomItem.byId(id.javaValue())
                .map(CustomItem::toItemStack)
                .orElse(ItemStack.EMPTY));
    }

    @MethodTypeHint("(item_id: identifier, template: identifier) -> item")
    public static RItem item__templated(RIdentifier id, RIdentifier template) {
        return RItem.of(CustomItem.byId(id.javaValue())
                .map(x -> x.toItemStack(template.javaValue()))
                .orElse(ItemStack.EMPTY));
    }

    @MethodTypeHint("(items: list[item]?, name: text?) -> inventory")
    public static RInventory inventory__create(RList itemList, RText name) {
        var inv = RInventory.of(new DynamicContainer(27));
        if(itemList != null) {
            for(var item : itemList.javaValue()) {
                if(item instanceof RItem item1) {
                    RInventory.add_item(inv, item1);
                }
            }
        }
        if(name != null) {
            RInventory.set_name(inv, name);
        }
        return inv;
    }

    @MethodTypeHint("(key: string) -> store")
    public static RStore store__temp(RString key) {
        return RStore.of(Database.temp().get(key.javaValue()));
    }

    @MethodTypeHint("(key: string) -> store")
    public static RStore store__save(RString key) {
        return RStore.of(Database.temp().get(key.javaValue()));
    }

    @MethodTypeHint("<T>(this: T) -> nullable[T]")
    public static RNullable nullable__of(RuntimeValue<?> any) {
        return RNullable.of(any);
    }

    @MethodTypeHint("() -> nullable[any]")
    public static RNullable nullable__empty() {
        return RNullable.of(null);
    }
}
