package dev.akarah.cdata.script.expr.item;

import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.stat.StatsObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;

public class ItemUtil {
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
}
