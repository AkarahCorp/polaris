package dev.akarah.cdata.registry;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.codec.MetaCodec;
import dev.akarah.cdata.registry.citem.CustomItem;
import dev.akarah.cdata.property.Property;
import dev.akarah.cdata.registry.text.TextElement;
import dev.akarah.cdata.script.action.ActionProvider;
import dev.akarah.cdata.script.value.ValueProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ExtRegistries {
    public static ResourceKey<Registry<CustomItem>> CUSTOM_ITEM =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("citem"));
    public static ResourceKey<Registry<Property<?>>> PROPERTIES =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("property"));
    public static ResourceKey<Registry<MapCodec<? extends MetaCodec<?>>>> META_CODEC_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("meta_codec_type"));
    public static ResourceKey<Registry<MetaCodec<?>>> META_CODEC =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("meta_codec"));
    public static ResourceKey<Registry<TextElement>> TEXT_ELEMENT =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("text_element"));

    public static ResourceKey<Registry<MapCodec<? extends ValueProvider>>> VALUE_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("action/value_type"));
    public static ResourceKey<Registry<MapCodec<? extends ActionProvider>>> ACTION_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("action/action_type"));
    public static ResourceKey<Registry<ActionProvider>> SCRIPT =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("action"));
}
