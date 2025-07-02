package dev.akarah.cdata.registry.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.registry.ExtReloadableResources;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

import java.util.List;

public record TextElement(
        List<ParsedText> lines
) {
    public static Codec<TextElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ParsedText.CODEC.listOf().fieldOf("lines").forGetter(TextElement::lines)
    ).apply(instance, TextElement::new));

    public static Codec<TextElement> CODEC_BY_ID =
            Codec.lazyInitialized(() -> ExtReloadableResources.textElement().registry().byNameCodec());
}
