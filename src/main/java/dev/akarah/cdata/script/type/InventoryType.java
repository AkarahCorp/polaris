package dev.akarah.cdata.script.type;

import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RInventory;
import dev.akarah.cdata.script.value.RText;

import java.lang.constant.ClassDesc;

public record InventoryType() implements Type<RInventory> {
    @Override
    public String typeName() {
        return "inventory";
    }

    @Override
    public Class<RInventory> typeClass() {
        return RInventory.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RInventory.class);
    }
}
