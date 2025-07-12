package dev.akarah.cdata.registry;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.codec.MetaCodec;
import dev.akarah.cdata.registry.entity.behavior.TaskType;
import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;

import java.util.List;

public class ExtBuiltInRegistries {
    public static Registry<MapCodec<? extends MetaCodec<?>>> META_CODEC_TYPE;
    public static Registry<MapCodec<? extends TaskType>> BEHAVIOR_TYPE;

    public static void bootStrap() {
        ExtBuiltInRegistries.META_CODEC_TYPE = BuiltInRegistries.registerSimple(ExtRegistries.META_CODEC_TYPE, MetaCodec::bootStrapTypes);
        ExtBuiltInRegistries.BEHAVIOR_TYPE = BuiltInRegistries.registerSimple(ExtRegistries.BEHAVIOR_TYPE, TaskType::bootStrap);
    }

    public static List<RegistryDataLoader.RegistryData<?>> DYNAMIC_REGISTRIES = List.of(
            new RegistryDataLoader.RegistryData<>(ExtRegistries.META_CODEC, MetaCodec.CODEC, false)
    );
}
