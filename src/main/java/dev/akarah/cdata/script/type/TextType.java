package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RText;

import java.lang.constant.ClassDesc;

public record TextType() implements Type<RText> {
    @Override
    public String typeName() {
        return "text";
    }

    @Override
    public Class<RText> typeClass() {
        return RText.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RText.class);
    }
}
