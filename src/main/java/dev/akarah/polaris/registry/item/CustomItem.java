package dev.akarah.polaris.registry.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.item.value.CustomItemComponents;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.Map;
import java.util.Optional;

public record CustomItem(
        Identifier model,
        Optional<String> name,
        Optional<StatsObject> stats,
        Optional<CustomItemComponents> components,
        Optional<Identifier> itemTemplate,
        Optional<Map<String, RuntimeValue>> customData
) {
    public static Codec<CustomItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("model").forGetter(CustomItem::model),
            Codec.STRING.optionalFieldOf("name").forGetter(CustomItem::name),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomItem::stats),
            CustomItemComponents.CODEC.optionalFieldOf("components").forGetter(CustomItem::components),
            Identifier.CODEC.optionalFieldOf("item_template").forGetter(CustomItem::itemTemplate),
            Codec.unboundedMap(Codec.STRING, RuntimeValue.CODEC).optionalFieldOf("custom_data").forGetter(CustomItem::customData)
    ).apply(instance, CustomItem::new));

    public static Codec<CustomItem> CODEC_BY_ID =
            Codec.lazyInitialized(() -> Resources.customItem().registry().byNameCodec());

    public Identifier id() {
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

        var placesAs = this.components.map(CustomItemComponents::placesBlock).orElse(Identifier.withDefaultNamespace(""));

        if(BuiltInRegistries.ITEM.containsKey(placesAs)) {
            item = BuiltInRegistries.ITEM.get(placesAs).orElseThrow().value();
        }
        var is = new ItemStack(Holder.direct(item));
        is.setCount(amount);

        var cdata = new CompoundTag();

        if(customData != null) {
            cdata.merge(customData.copyTag());
        }
        cdata.put("id", StringTag.valueOf(this.id().toString()));
        is.set(DataComponents.CUSTOM_DATA, CustomData.of(cdata));
        is.set(DataComponents.MAX_STACK_SIZE, this.components.map(CustomItemComponents::maxStackSize).orElse(1));
        return is;
    }

    public ItemStack toItemStack(Identifier itemTemplate, RNullable entity, CustomData customData, int amount) {
        var item = Items.MUSIC_DISC_CAT;

        var placesAs = this.components.map(CustomItemComponents::placesBlock).orElse(Identifier.withDefaultNamespace(""));

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

        if(customData != null) {
            cdata.merge(customData.copyTag());
        }
        cdata.put("id", StringTag.valueOf(this.id().toString()));
        is.set(DataComponents.CUSTOM_DATA, CustomData.of(cdata));

        is.set(DataComponents.ITEM_MODEL, this.model());

        var display = TooltipDisplay.DEFAULT
                .withHidden(DataComponents.TRIM, true)
                .withHidden(DataComponents.DYED_COLOR, true);
        if(this.components().map(CustomItemComponents::hideTooltip).orElse(false)) {
            display = new TooltipDisplay(true, display.hiddenComponents());
        }
        is.set(
                DataComponents.TOOLTIP_DISPLAY,
                display
        );
        this.components().flatMap(CustomItemComponents::equippable).ifPresent(equippableData -> {
            is.set(DataComponents.EQUIPPABLE, equippableData.component());
        });
        this.components().flatMap(CustomItemComponents::color).ifPresent(dyedItemColor -> {
            is.set(DataComponents.DYED_COLOR, dyedItemColor);
        });
        this.components().flatMap(CustomItemComponents::trim).ifPresent(trim -> {
            try {
                is.set(DataComponents.TRIM, new ArmorTrim(
                        Main.server().registryAccess().lookup(Registries.TRIM_MATERIAL).orElseThrow()
                                .get(trim.material()).orElseThrow(),
                        Main.server().registryAccess().lookup(Registries.TRIM_PATTERN).orElseThrow()
                                .get(trim.pattern()).orElseThrow()
                ));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.components().flatMap(CustomItemComponents::profile).ifPresent(profile -> {
            is.set(DataComponents.PROFILE, profile);
        });
        this.components().flatMap(CustomItemComponents::blocksAttacks).ifPresent(blocksAttacks -> {
            is.set(DataComponents.BLOCKS_ATTACKS, blocksAttacks);
            is.set(DataComponents.WEAPON, new Weapon(0, 0));
        });
        this.components().flatMap(CustomItemComponents::customModelData).ifPresent(customModelData -> {
            is.set(DataComponents.CUSTOM_MODEL_DATA, customModelData);
        });
        is.set(DataComponents.SWING_ANIMATION, this.components().map(CustomItemComponents::swingAnimation).orElse(SwingAnimation.DEFAULT));
        is.set(DataComponents.MAX_STACK_SIZE, this.components.map(CustomItemComponents::maxStackSize).orElse(1));
        is.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, this.components.map(CustomItemComponents::overrideEnchantmentGlint).orElse(false));
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

    public static Optional<Identifier> itemIdOf(ItemStack itemStack) {
        if(itemStack == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(itemStack.get(DataComponents.CUSTOM_DATA))
                .map(CustomData::copyTag)
                .map(x -> x.get("id"))
                .flatMap(Tag::asString)
                .map(Identifier::parse);
    }

    public static Optional<CustomItem> itemOf(ItemStack itemStack) {
        return itemIdOf(itemStack)
                .flatMap(x -> Resources.customItem().registry().get(x))
                .map(Holder.Reference::value);
    }

    public static Optional<CustomItem> byId(Identifier id) {
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
