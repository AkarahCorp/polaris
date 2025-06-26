package dev.akarah.cdata.property;

import com.mojang.serialization.Codec;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.property.value.CustomData;
import dev.akarah.cdata.property.value.EquippableData;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.registry.text.ParseContext;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.registry.text.TextElement;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.Equippable;

import java.util.Optional;
import java.util.function.UnaryOperator;

public class Properties {
    public static Property<ParsedText> NAME = register(
            "name",
            builder -> builder
                    .codec(ParsedText.CODEC)
                    .itemApplication((item, value, map) -> {
                        var component = value.output(ParseContext.item(map)).orElseGet(Component::empty);
                        item.set(DataComponents.ITEM_NAME, component);
                    })
    );

    public static Property<ResourceLocation> MODEL = register(
            "model",
            builder -> builder
                    .codec(ResourceLocation.CODEC)
                    .itemApplication((item, value) -> item.set(DataComponents.ITEM_MODEL, value))
    );

    public static Property<StatsObject> STATS = register(
            "stats",
            builder -> builder
                    .codec(StatsObject.CODEC)
    );

    public static Property<EquippableData> EQUIPPABLE = register(
            "equippable",
            builder -> builder
                    .codec(EquippableData.CODEC)
                    .itemApplication((item, value) -> item.set(DataComponents.EQUIPPABLE, value.component()))
    );

    public static Property<Holder<TextElement>> ITEM_TEMPLATE = register(
            "item_template",
            builder -> builder
                    .codec(RegistryFileCodec.create(ExtRegistries.TEXT_ELEMENT, TextElement.CODEC))
                    .itemApplication((item, value, map) -> {
                        var environment = ParseContext.item(map);
                        item.set(DataComponents.LORE, new ItemLore(
                                value.value().lines()
                                        .stream()
                                        .map(x -> x.output(environment))
                                        .flatMap(Optional::stream)
                                        .toList()
                        ));
                    })
    );

    public static Property<CustomData> CUSTOM_DATA = register(
            "custom_data",
            builder -> builder
                    .codec(CustomData.CODEC)
                    .itemApplication((item, value) -> item.set(
                            DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.of(
                                    Codec.PASSTHROUGH
                                            .encodeStart(NbtOps.INSTANCE, value.value().convert(NbtOps.INSTANCE))
                                            .getOrThrow()
                                            .asCompound()
                                            .orElse(new CompoundTag())
                            )
                    ))
    );

    private static <T> Property<T> register(String name, UnaryOperator<Property.Builder<T>> operator) {
        return Registry.register(ExtBuiltInRegistries.PROPERTIES, name, operator.apply(Property.builder()).build());
    }
}
