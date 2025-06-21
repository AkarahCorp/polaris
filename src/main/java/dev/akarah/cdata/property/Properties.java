package dev.akarah.cdata.property;

import com.mojang.serialization.Codec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.UnaryOperator;

public class Properties {
    public static Property<String> NAME = register(
            "name",
            builder -> builder
                    .codec(Codec.STRING)
                    .itemApplication((item, value) -> item.set(DataComponents.ITEM_NAME, Component.literal(value)))
    );

    public static Property<ResourceLocation> MODEL = register(
            "model",
            builder -> builder
                    .codec(ResourceLocation.CODEC)
                    .itemApplication((item, value) -> item.set(DataComponents.ITEM_MODEL, value))
    );

    private static <T> Property<T> register(String name, UnaryOperator<Property.Builder<T>> operator) {
        return Registry.register(ExtBuiltInRegistries.PROPERTIES, name, operator.apply(Property.builder()).build());
    }
}
