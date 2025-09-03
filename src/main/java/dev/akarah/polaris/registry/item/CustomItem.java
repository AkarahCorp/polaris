package dev.akarah.polaris.registry.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.item.value.CustomComponents;
import dev.akarah.polaris.registry.stat.StatsObject;
import dev.akarah.polaris.script.value.RNullable;
import dev.akarah.polaris.script.value.RStatsObject;
import dev.akarah.polaris.script.value.RuntimeValue;
import dev.akarah.polaris.script.value.mc.RItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.Map;
import java.util.Optional;

public record CustomItem(
        ResourceLocation model,
        Optional<String> name,
        Optional<StatsObject> stats,
        Optional<CustomComponents> components,
        Optional<ResourceLocation> itemTemplate,
        Optional<Map<String, RuntimeValue>> customData
) {
    public static Codec<CustomItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("model").forGetter(CustomItem::model),
            Codec.STRING.optionalFieldOf("name").forGetter(CustomItem::name),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomItem::stats),
            CustomComponents.CODEC.optionalFieldOf("components").forGetter(CustomItem::components),
            ResourceLocation.CODEC.optionalFieldOf("item_template").forGetter(CustomItem::itemTemplate),
            Codec.unboundedMap(Codec.STRING, RuntimeValue.CODEC).optionalFieldOf("custom_data").forGetter(CustomItem::customData)
    ).apply(instance, CustomItem::new));

    public static Codec<CustomItem> CODEC_BY_ID =
            Codec.lazyInitialized(() -> Resources.customItem().registry().byNameCodec());

    public ResourceLocation id() {
        return Resources
                .customItem()
                .registry()
                .getKey(this);
    }

    public ItemStack toItemStack(RNullable entity) {
        return this.toItemStack(this.itemTemplate.orElse(null), entity, null, 1);
    }

    public ItemStack toItemStack(RNullable entity, CustomData customData, int amount) {
        return this.toItemStack(this.itemTemplate.orElse(null), entity, customData, amount);
    }

    public ItemStack toMinimalItemStack(CustomData customData, int amount) {
        var item = Items.MUSIC_DISC_CAT;

        var placesAs = this.components.map(CustomComponents::placesBlock).orElse(ResourceLocation.withDefaultNamespace(""));

        if(BuiltInRegistries.ITEM.containsKey(placesAs)) {
            item = BuiltInRegistries.ITEM.get(placesAs).orElseThrow().value();
        }
        var is = new ItemStack(Holder.direct(item));
        is.setCount(amount);

        var cdata = new CompoundTag();
        cdata.put("id", StringTag.valueOf(this.id().toString()));

        if(customData != null) {
            cdata.merge(customData.getUnsafe());
        }
        is.set(DataComponents.CUSTOM_DATA, CustomData.of(cdata));
        is.set(DataComponents.MAX_STACK_SIZE, this.components.map(CustomComponents::maxStackSize).orElse(1));
        return is;
    }

    public ItemStack toItemStack(ResourceLocation itemTemplate, RNullable entity, CustomData customData, int amount) {
        var item = Items.MUSIC_DISC_CAT;

        var placesAs = this.components.map(CustomComponents::placesBlock).orElse(ResourceLocation.withDefaultNamespace(""));

        if(BuiltInRegistries.ITEM.containsKey(placesAs)) {
            item = BuiltInRegistries.ITEM.get(placesAs).orElseThrow().value();
        }
        var is = new ItemStack(Holder.direct(item));
        is.setCount(amount);
        is.remove(DataComponents.JUKEBOX_PLAYABLE);
        is.remove(DataComponents.ITEM_MODEL);
        is.remove(DataComponents.ITEM_MODEL);
        is.remove(DataComponents.MAX_STACK_SIZE);

        var cdata = new CompoundTag();
        cdata.put("id", StringTag.valueOf(this.id().toString()));

        if(customData != null) {
            cdata.merge(customData.getUnsafe());
        }
        is.set(DataComponents.CUSTOM_DATA, CustomData.of(cdata));

        is.set(DataComponents.ITEM_MODEL, this.model());

        var display = TooltipDisplay.DEFAULT
                .withHidden(DataComponents.TRIM, true)
                .withHidden(DataComponents.DYED_COLOR, true);
        if(this.components().map(CustomComponents::hideTooltip).orElse(false)) {
            display = new TooltipDisplay(true, display.hiddenComponents());
        }
        is.set(
                DataComponents.TOOLTIP_DISPLAY,
                display
        );
        this.components().flatMap(CustomComponents::equippable).ifPresent(equippableData -> {
            is.set(DataComponents.EQUIPPABLE, equippableData.component());
        });
        this.components().flatMap(CustomComponents::color).ifPresent(dyedItemColor -> {
            is.set(DataComponents.DYED_COLOR, dyedItemColor);
        });
        this.components().flatMap(CustomComponents::trim).ifPresent(trim -> {
            try {
                is.set(DataComponents.TRIM, new ArmorTrim(
                        Main.server().registryAccess().lookup(Registries.TRIM_MATERIAL).orElseThrow()
                                .get(trim.material()).orElseThrow(),
                        Main.server().registryAccess().lookup(Registries.TRIM_PATTERN).orElseThrow()
                                .get(trim.pattern()).orElseThrow()
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.components().flatMap(CustomComponents::playerSkin).ifPresent(playerSkinUuid -> {
            var gp = Resources.GAME_PROFILES.get(playerSkinUuid);
            if(gp != null) {
                is.set(DataComponents.PROFILE, new ResolvableProfile(gp));
            }
        });
        this.components().flatMap(CustomComponents::blocksAttacks).ifPresent(blocksAttacks -> {
            is.set(DataComponents.BLOCKS_ATTACKS, blocksAttacks);
            is.set(DataComponents.WEAPON, new Weapon(0, 0));
        });
        this.components().flatMap(CustomComponents::customModelData).ifPresent(customModelData -> {
            is.set(DataComponents.CUSTOM_MODEL_DATA, customModelData);
        });
        is.set(DataComponents.MAX_STACK_SIZE, this.components.map(CustomComponents::maxStackSize).orElse(1));
        is.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, this.components.map(CustomComponents::overrideEnchantmentGlint).orElse(false));
        if(itemTemplate != null) {
            try {
                Resources.actionManager().executeVoid(
                        itemTemplate,
                        RItem.of(is),
                        entity
                );
            } catch (Throwable _) {

            }
        }

        return is;
    }

    public static Optional<ResourceLocation> itemIdOf(ItemStack itemStack) {
        if(itemStack == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(itemStack.get(DataComponents.CUSTOM_DATA))
                .map(CustomData::copyTag)
                .map(x -> x.get("id"))
                .flatMap(Tag::asString)
                .map(ResourceLocation::parse);
    }

    public static Optional<CustomItem> itemOf(ItemStack itemStack) {
        return itemIdOf(itemStack)
                .flatMap(x -> Resources.customItem().registry().get(x))
                .map(Holder.Reference::value);
    }

    public static Optional<CustomItem> byId(ResourceLocation id) {
        return Resources.customItem().registry().get(id).map(Holder.Reference::value);
    }

    public StatsObject modifiedStats(RNullable entity, ItemStack itemStack) {
        var so = RStatsObject.of(this.stats.orElse(StatsObject.of()).copy().withRenamedSources(itemStack.getDisplayName()));

        Resources.actionManager().performEvents(
                "item.get_stats",
                RItem.of(itemStack),
                entity,
                so
        );
        return so.javaValue();
    }
}
