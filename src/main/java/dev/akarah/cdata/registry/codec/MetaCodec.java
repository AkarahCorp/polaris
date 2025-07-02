package dev.akarah.cdata.registry.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import net.minecraft.core.Registry;

public interface MetaCodec<T> {
    Codec<MetaCodec<?>> CODEC =
            Codec.lazyInitialized(() -> ExtBuiltInRegistries
                    .META_CODEC_TYPE
                    .byNameCodec()
                    .dispatch(MetaCodec::generatorCodec, (mapCodec) -> mapCodec));

    Codec<T> codec();
    MapCodec<? extends MetaCodec<T>> generatorCodec();

    static MapCodec<? extends MetaCodec<?>> bootStrapTypes(Registry<MapCodec<? extends MetaCodec<?>>> registry) {
        var intCodecType = Registry.register(
                registry,
                "int",
                IntCodec.GENERATOR_CODEC
        );
        Registry.register(
                registry,
                "string",
                StringCodec.GENERATOR_CODEC
        );
        Registry.register(
                registry,
                "object",
                ObjectCodec.GENERATOR_CODEC
        );

        return intCodecType;
    }
}
