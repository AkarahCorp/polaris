package dev.akarah.cdata.registry.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.registry.ExtRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

import java.util.List;

public record TextElement(
        List<ParsedText> lines
) {
    public static Codec<TextElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ParsedText.CODEC.listOf().fieldOf("lines").forGetter(TextElement::lines)
    ).apply(instance, TextElement::new));

    public static Codec<Holder<TextElement>> HOLDER_CODEC = RegistryFileCodec.create(ExtRegistries.TEXT_ELEMENT, TextElement.CODEC);
}
