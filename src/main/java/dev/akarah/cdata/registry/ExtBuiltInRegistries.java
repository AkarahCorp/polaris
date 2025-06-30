package dev.akarah.cdata.registry;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.codec.MetaCodec;
import dev.akarah.cdata.registry.entity.CustomEntity;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.text.TextElement;
import dev.akarah.cdata.script.action.CompilableAction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;

import java.util.List;

public class ExtBuiltInRegistries {
    public static Registry<MapCodec<? extends MetaCodec<?>>> META_CODEC_TYPE;
    public static Registry<MapCodec<? extends CompilableAction>> ACTION_TYPE;

    public static void bootStrap() {
        ExtBuiltInRegistries.META_CODEC_TYPE = BuiltInRegistries.registerSimple(ExtRegistries.META_CODEC_TYPE, MetaCodec::bootStrapTypes);
        ExtBuiltInRegistries.ACTION_TYPE = BuiltInRegistries.registerSimple(ExtRegistries.ACTION_TYPE, CompilableAction::bootStrap);
    }

    public static List<RegistryDataLoader.RegistryData<?>> DYNAMIC_REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(ExtRegistries.CUSTOM_ITEM, CustomItem.CODEC, false),
            new RegistryDataLoader.RegistryData<>(ExtRegistries.CUSTOM_ENTITY, CustomEntity.CODEC, false),
            new RegistryDataLoader.RegistryData<>(ExtRegistries.META_CODEC, MetaCodec.DIRECT_CODEC, false),
            new RegistryDataLoader.RegistryData<>(ExtRegistries.TEXT_ELEMENT, TextElement.CODEC, false),
            new RegistryDataLoader.RegistryData<>(ExtRegistries.SCRIPT, CompilableAction.CODEC, false)
    );
}
