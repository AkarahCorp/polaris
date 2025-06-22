package dev.akarah.cdata.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public record StringCodec() implements MetaCodec<String> {
    public static MapCodec<StringCodec> GENERATOR_CODEC = MapCodec.unit(StringCodec::new);

    @Override
    public Codec<String> codec() {
        return Codec.STRING;
    }

    @Override
    public MapCodec<? extends MetaCodec<String>> generatorCodec() {
        return GENERATOR_CODEC;
    }

    @Override
    public @NotNull String toString() {
        return "string";
    }
}
