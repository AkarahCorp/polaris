package dev.akarah.cdata.registry.citem;

import com.mojang.serialization.Codec;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.property.PropertyMap;
import dev.akarah.cdata.registry.ExtRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public record CustomItem(
        PropertyMap properties
) {
    public static Codec<CustomItem> CODEC = PropertyMap.CODEC.xmap(CustomItem::new, CustomItem::properties);

    public ResourceLocation id() {
        return Main.server()
                .registryAccess()
                .lookupOrThrow(ExtRegistries.CUSTOM_ITEM)
                .getKey(this);
    }

    public ItemStack toItemStack() {
        var is = new ItemStack(Holder.direct(Items.MUSIC_DISC_CAT));
        is.remove(DataComponents.JUKEBOX_PLAYABLE);
        is.remove(DataComponents.ITEM_MODEL);
        is.remove(DataComponents.ITEM_MODEL);
        is.remove(DataComponents.MAX_STACK_SIZE);

        var cdata = new CompoundTag();
        cdata.put("id", StringTag.valueOf(this.id().toString()));
        is.set(DataComponents.CUSTOM_DATA, CustomData.of(cdata));
        is.setCount(1);

        for(var property : this.properties().keySet()) {
            property.applyToItemUnchecked(is, this.properties.get(property).orElseThrow(), this.properties);
        }

        return is;
    }

    public static Optional<ResourceLocation> itemIdOf(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.get(DataComponents.CUSTOM_DATA))
                .map(CustomData::copyTag)
                .map(x -> x.get("id"))
                .flatMap(Tag::asString)
                .map(ResourceLocation::parse);
    }

    public static Optional<CustomItem> itemOf(ItemStack itemStack) {
        return itemIdOf(itemStack)
                .flatMap(x -> Main.server().registryAccess().lookupOrThrow(ExtRegistries.CUSTOM_ITEM).get(x))
                .map(Holder.Reference::value);
    }
}
