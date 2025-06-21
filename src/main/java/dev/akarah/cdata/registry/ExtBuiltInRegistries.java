package dev.akarah.cdata.registry;

import com.mojang.serialization.Lifecycle;
import dev.akarah.cdata.registry.citem.CustomItem;
import dev.akarah.cdata.property.Property;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;

import java.util.List;

public class ExtBuiltInRegistries {
    public static Registry<Property<?>> PROPERTIES = new MappedRegistry<>(ExtRegistries.PROPERTIES, Lifecycle.experimental());

    public static List<RegistryDataLoader.RegistryData<?>> DYNAMIC_REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(ExtRegistries.CUSTOM_ITEM, CustomItem.CODEC, false)
    );
}
