package dev.akarah.cdata.script.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RDict;
import dev.akarah.cdata.script.value.RFunction;

import java.lang.constant.ClassDesc;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public record FunctionType(
        Type<?> returnType,
        List<? extends Type<?>> parameterTypes
) implements Type<RFunction> {
    @Override
    public String typeName() {
        return "function";
    }

    @Override
    public Class<RFunction> typeClass() {
        return RFunction.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RFunction.class);
    }

    @Override
    public List<Type<?>> subtypes() {
        return Stream.concat(Stream.of(returnType), parameterTypes.stream())
                .toList();
    }
}
