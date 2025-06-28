package dev.akarah.cdata.registry;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.codec.MetaCodec;
import dev.akarah.cdata.registry.entity.CustomEntity;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.text.TextElement;
import dev.akarah.cdata.script.action.ActionProvider;
import dev.akarah.cdata.script.value.ValueProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ExtRegistries {
    public static ResourceKey<Registry<CustomItem>> CUSTOM_ITEM =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "item"));
    public static ResourceKey<Registry<CustomEntity>> CUSTOM_ENTITY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "entity"));
    public static ResourceKey<Registry<MapCodec<? extends MetaCodec<?>>>> META_CODEC_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "codec/type"));
    public static ResourceKey<Registry<MetaCodec<?>>> META_CODEC =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "codec"));
    public static ResourceKey<Registry<TextElement>> TEXT_ELEMENT =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "text"));

    public static ResourceKey<Registry<MapCodec<? extends ValueProvider>>> VALUE_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "action/value_type"));
    public static ResourceKey<Registry<MapCodec<? extends ActionProvider>>> ACTION_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "action/action_type"));
    public static ResourceKey<Registry<ActionProvider>> SCRIPT =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "action"));
}
