package dev.akarah.cdata.script.type;

import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.jvm.CodegenUtil;

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
        return CodegenUtil.ofClass(ParsedText.class);
    }
}
