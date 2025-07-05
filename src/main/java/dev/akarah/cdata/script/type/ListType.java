package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

public record ListType() implements Type<ArrayList<Object>> {
    @Override
    public String typeName() {
        return "list";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ArrayList<Object>> typeClass() {
        return (Class<ArrayList<Object>>) (Class<?>) List.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(ArrayList.class);
    }
}
