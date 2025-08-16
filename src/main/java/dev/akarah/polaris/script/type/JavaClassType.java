package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;

import java.lang.constant.ClassDesc;

public record JavaClassType<T>(Class<T> clazz, String typeName) implements Type<T> {

    @Override
    public Class<T> typeClass() {
        return clazz;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(this.clazz);
    }
}
