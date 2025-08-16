package dev.akarah.polaris.registry.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record TexturesData(
        long timestamp,
        String profileId,
        String profileName,
        Textures textures,
        boolean signatureRequired
) {
        public static Codec<TexturesData> TEXTURES = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("timestamp").forGetter(TexturesData::timestamp),
                Codec.STRING.fieldOf("profileId").forGetter(TexturesData::profileId),
                Codec.STRING.fieldOf("profileName").forGetter(TexturesData::profileName),
                Textures.CODEC.fieldOf("textures").forGetter(TexturesData::textures),
                Codec.BOOL.fieldOf("signatureRequired").forGetter(TexturesData::signatureRequired)
        ).apply(instance, TexturesData::new));

        public record Textures(
                Optional<UrlHolder> skinTexture,
                Optional<UrlHolder> capeTexture
        ) {
                public static Codec<Textures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        UrlHolder.CODEC.optionalFieldOf("SKIN").forGetter(Textures::skinTexture),
                        UrlHolder.CODEC.optionalFieldOf("CAPE").forGetter(Textures::capeTexture)
                ).apply(instance, Textures::new));
        }

        public record UrlHolder(String url) {
                public static Codec<UrlHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.STRING.fieldOf("url").forGetter(UrlHolder::url)
                ).apply(instance, UrlHolder::new));
        }
}
