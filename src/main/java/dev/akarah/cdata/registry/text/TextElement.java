package dev.akarah.cdata.registry.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record TextElement(
        List<ParsedText> lines
) {
    public static Codec<TextElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ParsedText.CODEC.listOf().fieldOf("lines").forGetter(TextElement::lines)
    ).apply(instance, TextElement::new));
}
