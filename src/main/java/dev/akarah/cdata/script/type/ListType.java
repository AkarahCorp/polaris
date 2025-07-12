package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RList;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

public record ListType(Type<?> subtype) implements Type<RList> {
    @Override
    public String typeName() {
        return "list";
    }

    @Override
    public Class<RList> typeClass() {
        return RList.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RList.class);
    }

    @Override
    public List<Type<?>> subtypes() {
        var a = new ArrayList<Type<?>>();
        a.add(subtype);
        return a;
    }
}
