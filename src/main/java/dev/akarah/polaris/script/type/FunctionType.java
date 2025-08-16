package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RFunction;

import java.lang.constant.ClassDesc;
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
