package dev.akarah.cdata.registry;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.codec.MetaCodec;
import dev.akarah.cdata.registry.entity.behavior.TaskType;
import dev.akarah.cdata.registry.text.TextElement;
import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ExtRegistries {
    public static ResourceKey<Registry<MapCodec<? extends MetaCodec<?>>>> META_CODEC_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "codec/type"));
    public static ResourceKey<Registry<MetaCodec<?>>> META_CODEC =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "codec"));
    public static ResourceKey<Registry<TextElement>> TEXT_ELEMENT =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "text"));
    public static ResourceKey<Registry<Class<? extends Expression>>> ACTION_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "script/action/type"));
    public static ResourceKey<Registry<MapCodec<? extends TaskType>>> BEHAVIOR_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "behavior/type"));
}
