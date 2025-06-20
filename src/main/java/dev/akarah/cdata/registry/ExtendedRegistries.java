package dev.akarah.cdata.registry;

import dev.akarah.cdata.registry.citem.CustomItem;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ExtendedRegistries {
    public static ResourceKey<Registry<CustomItem>> CUSTOM_ITEM = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("citem"));

    public static List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(ExtendedRegistries.CUSTOM_ITEM, CustomItem.CODEC, false)
    );

}
