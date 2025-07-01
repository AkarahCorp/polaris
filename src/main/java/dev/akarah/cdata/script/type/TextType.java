package dev.akarah.cdata.script.type;

import dev.akarah.cdata.registry.text.ParsedText;

public record TextType() implements Type<ParsedText> {
    @Override
    public String typeName() {
        return "text";
    }

    @Override
    public Class<ParsedText> typeClass() {
        return ParsedText.class;
    }
}
