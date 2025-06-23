package dev.akarah.cdata.script.value;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.value.values.NumberValue;
import dev.akarah.cdata.script.value.values.StringValue;
import dev.akarah.cdata.script.value.values.TextLineValue;
import net.minecraft.core.Registry;

public class ValueTypes {
    public static MapCodec<? extends ValueProvider> bootStrap(Registry<MapCodec<? extends ValueProvider>> registry) {
        Registry.register(
                registry,
                "text",
                TextLineValue.GENERATOR_CODEC
        );

        Registry.register(
                registry,
                "string",
                StringValue.GENERATOR_CODEC
        );

        Registry.register(
                registry,
                "number",
                NumberValue.GENERATOR_CODEC
        );

        return TextLineValue.GENERATOR_CODEC;
    }
}
