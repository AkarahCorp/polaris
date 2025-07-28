package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RCell;
import dev.akarah.cdata.script.value.RList;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

public record CellType(Type<?> subtype) implements Type<RCell> {
    @Override
    public String typeName() {
        return "cell";
    }

    @Override
    public Class<RCell> typeClass() {
        return RCell.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RCell.class);
    }

    @Override
    public List<Type<?>> subtypes() {
        var a = new ArrayList<Type<?>>();
        a.add(subtype);
        return a;
    }
}
