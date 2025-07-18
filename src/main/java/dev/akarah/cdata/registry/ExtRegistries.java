package dev.akarah.cdata.registry;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.entity.behavior.TaskType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ExtRegistries {
    public static ResourceKey<Registry<MapCodec<? extends TaskType>>> BEHAVIOR_TYPE =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("engine", "behavior/type"));
}
