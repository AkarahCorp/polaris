package dev.akarah.cdata.registry;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.codec.MetaCodec;
import dev.akarah.cdata.property.Properties;
import dev.akarah.cdata.registry.citem.CustomItem;
import dev.akarah.cdata.property.Property;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;

import java.util.List;

public class ExtBuiltInRegistries {
    public static Registry<Property<?>> PROPERTIES;
    public static Registry<MapCodec<? extends MetaCodec<?>>> META_CODEC_TYPE;

    public static void bootStrap() {
        ExtBuiltInRegistries.PROPERTIES = BuiltInRegistries.registerSimple(ExtRegistries.PROPERTIES, (registry) -> Properties.NAME);
        ExtBuiltInRegistries.META_CODEC_TYPE = BuiltInRegistries.registerSimple(ExtRegistries.META_CODEC_TYPE, MetaCodec::bootStrapTypes);
    }

    public static List<RegistryDataLoader.RegistryData<?>> DYNAMIC_REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(ExtRegistries.CUSTOM_ITEM, CustomItem.CODEC, false),
            new RegistryDataLoader.RegistryData<>(ExtRegistries.META_CODEC, MetaCodec.DIRECT_CODEC, false)
    );
}
