package dev.akarah.cdata.registry.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public record IntCodec() implements MetaCodec<Integer> {
    public static MapCodec<IntCodec> GENERATOR_CODEC = MapCodec.unit(IntCodec::new);

    @Override
    public Codec<Integer> codec() {
        return Codec.INT;
    }

    @Override
    public MapCodec<? extends MetaCodec<Integer>> generatorCodec() {
        return GENERATOR_CODEC;
    }

    @Override
    public @NotNull String toString() {
        return "int";
    }
}
