package dev.akarah.cdata.registry;

import dev.akarah.cdata.registry.citem.CustomItem;
import dev.akarah.cdata.property.Property;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ExtRegistries {
    public static ResourceKey<Registry<CustomItem>> CUSTOM_ITEM = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("citem"));
    public static ResourceKey<Registry<Property<?>>> PROPERTIES = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("property"));
}
