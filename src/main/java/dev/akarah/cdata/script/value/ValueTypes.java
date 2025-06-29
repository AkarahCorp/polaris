package dev.akarah.cdata.script.value;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.value.values.constants.CustomItemValue;
import dev.akarah.cdata.script.value.values.constants.NumberValue;
import dev.akarah.cdata.script.value.values.constants.StringValue;
import dev.akarah.cdata.script.value.values.constants.TextLineValue;
import dev.akarah.cdata.script.value.values.env.LocalVarValue;
import dev.akarah.cdata.script.value.values.vec3.*;
import net.minecraft.core.Registry;

public class ValueTypes {
    public static MapCodec<? extends ValueProvider> bootStrap(Registry<MapCodec<? extends ValueProvider>> registry) {
        Registry.register(registry, "text", TextLineValue.GENERATOR_CODEC);
        Registry.register(registry, "string", StringValue.GENERATOR_CODEC);
        Registry.register(registry, "number", NumberValue.GENERATOR_CODEC);
        Registry.register(registry, "item", CustomItemValue.GENERATOR_CODEC);
        Registry.register(registry, "vec3", Vec3Value.GENERATOR_CODEC);
        Registry.register(registry, "local/load", LocalVarValue.GENERATOR_CODEC);
        Registry.register(registry, "entity/location", EntityVec3Value.GENERATOR_CODEC);
        Registry.register(registry, "entity/direction", EntityDirectionVec3Value.GENERATOR_CODEC);
        Registry.register(registry, "vec3/offset", OffsetVec3Value.GENERATOR_CODEC);
        Registry.register(registry, "vec3/multiply", MultiplyVec3Value.GENERATOR_CODEC);
        Registry.register(registry, "vec3/x", Vec3XValue.GENERATOR_CODEC);
        Registry.register(registry, "vec3/y", Vec3YValue.GENERATOR_CODEC);
        Registry.register(registry, "vec3/z", Vec3ZValue.GENERATOR_CODEC);
        return TextLineValue.GENERATOR_CODEC;
    }
}
