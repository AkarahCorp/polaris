package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RStruct;
import org.jetbrains.annotations.NotNull;

import java.lang.constant.ClassDesc;
import java.util.List;

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
