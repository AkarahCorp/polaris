package dev.akarah.cdata.registry.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.registry.item.value.EquippableData;
import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.registry.text.ParseContext;
import dev.akarah.cdata.registry.text.TextElement;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.Optional;

public record CustomItem(
        ResourceLocation model,
        Optional<String> name,
        Optional<StatsObject> stats,
        Optional<EquippableData> equippable,
        Optional<TextElement> itemTemplate,
        Optional<CustomData> customData
) {
    public static Codec<CustomItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("model").forGetter(CustomItem::model),
            Codec.STRING.optionalFieldOf("name").forGetter(CustomItem::name),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomItem::stats),
            EquippableData.CODEC.optionalFieldOf("equippable").forGetter(CustomItem::equippable),
            TextElement.CODEC_BY_ID.optionalFieldOf("item_template").forGetter(CustomItem::itemTemplate),
            CustomData.CODEC.optionalFieldOf("custom_data").forGetter(CustomItem::customData)
    ).apply(instance, CustomItem::new));

    public static Codec<CustomItem> CODEC_BY_ID =
            Codec.lazyInitialized(() -> ExtReloadableResources.customItem().registry().byNameCodec());

    public ResourceLocation id() {
        return ExtReloadableResources
                .customItem()
                .registry()
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

        is.set(DataComponents.ITEM_MODEL, this.model());
        this.equippable.ifPresent(equippableData -> {
            is.set(DataComponents.EQUIPPABLE, equippableData.component());
        });
        this.itemTemplate().ifPresent(itemTemplate -> {
            var environment = ParseContext.item(this);

            var lines = new ArrayList<>(
                    itemTemplate.lines()
                            .stream()
                            .map(x -> x.output(environment))
                            .flatMap(Optional::stream)
                            .toList()
            );
            is.set(DataComponents.ITEM_NAME, lines.removeFirst());
            is.set(DataComponents.LORE, new ItemLore(lines));
        });

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
                .flatMap(x -> ExtReloadableResources.customItem().registry().get(x))
                .map(Holder.Reference::value);
    }
}
