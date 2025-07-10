package dev.akarah.cdata.script.expr.item;

import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;

public class ItemUtil {
    public static void bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/create"), CreateItemExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/create_templated"), CreateTemplatedItemExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/set_lore"), SetItemLoreExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/set_name"), SetItemNameExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/name"), GetItemNameExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/stat"), GetItemStatExpression.class);
    }

    public static void setItemLore(ItemStack itemStack, ArrayList<?> components) {
        var lore = new ItemLore(
                components.stream()
                        .filter(x -> x instanceof Component)
                        .map(x -> (Component) x)
                        .toList()
        );
        itemStack.set(DataComponents.LORE, lore);
    }

    public static void setItemName(ItemStack itemStack, Component component) {
        itemStack.set(DataComponents.ITEM_NAME, component);
    }

    public static String getItemName(ItemStack itemStack) {
        return CustomItem.itemOf(itemStack)
                .flatMap(CustomItem::name)
                .orElseGet(() -> itemStack.getItemName().getString());
    }

    public static StatsObject getItemStats(ItemStack itemStack) {
        return CustomItem.itemOf(itemStack)
                .flatMap(CustomItem::stats)
                .orElse(StatsObject.EMPTY);
    }

    public static double getItemStat(ItemStack itemStack, String stat) {
        return getItemStats(itemStack).get(ResourceLocation.parse(stat));
    }

    public static ItemStack renderCustomItem(ResourceLocation base) {
        return CustomItem.byId(base)
                .map(CustomItem::toItemStack)
                .or(() -> BuiltInRegistries.ITEM.get(base).map(Holder.Reference::value).map(Item::getDefaultInstance))
                .orElse(ItemStack.EMPTY);
    }

    public static ItemStack renderItemTemplate(ResourceLocation base, ResourceLocation itemTemplate) {
        return CustomItem.byId(base)
                .map(x -> x.toItemStack(itemTemplate))
                .or(() -> BuiltInRegistries.ITEM.get(base).map(Holder.Reference::value).map(Item::getDefaultInstance))
                .orElse(ItemStack.EMPTY);
    }
}
