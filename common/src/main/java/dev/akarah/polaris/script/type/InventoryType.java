package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.mc.RInventory;

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
