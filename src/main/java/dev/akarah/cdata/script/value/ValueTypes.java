package dev.akarah.cdata.script.value;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public class ValueTypes {
    public static MapCodec<? extends ValueProvider> bootStrap(Registry<MapCodec<? extends ValueProvider>> registry) {
        Registry.register(
                registry,
                "text",
                TextLineValue.GENERATOR_CODEC
        );

        return TextLineValue.GENERATOR_CODEC;
    }
}
