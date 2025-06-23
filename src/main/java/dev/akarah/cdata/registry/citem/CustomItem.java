package dev.akarah.cdata.registry.citem;

import com.mojang.serialization.Codec;
import dev.akarah.cdata.property.PropertyMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record CustomItem(
        PropertyMap properties
) {
    public static Codec<CustomItem> CODEC = PropertyMap.CODEC.xmap(CustomItem::new, CustomItem::properties);

    public ItemStack toItemStack() {
        var is = new ItemStack(Holder.direct(Items.MUSIC_DISC_CAT));
        is.remove(DataComponents.JUKEBOX_PLAYABLE);
        is.remove(DataComponents.ITEM_MODEL);
        is.remove(DataComponents.ITEM_MODEL);
        is.remove(DataComponents.MAX_STACK_SIZE);
        is.setCount(1);

        for(var property : this.properties().keySet()) {
            property.applyToItemUnchecked(is, this.properties.get(property).orElseThrow(), this.properties);
        }

        return is;
    }
}
