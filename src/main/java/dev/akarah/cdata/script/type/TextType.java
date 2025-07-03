package dev.akarah.cdata.script.type;

import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.env.JIT;

import java.lang.constant.ClassDesc;

public record TextType() implements Type<ParsedText> {
    @Override
    public String typeName() {
        return "text";
    }

    @Override
    public Class<ParsedText> typeClass() {
        return ParsedText.class;
    }

    @Override
    public ClassDesc classDescType() {
        return JIT.ofClass(ParsedText.class);
    }
}
