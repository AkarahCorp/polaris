package dev.akarah.cdata.script.type;

import dev.akarah.cdata.db.DataStore;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RStore;
import net.minecraft.network.chat.Component;

import java.lang.constant.ClassDesc;

public record StoreType() implements Type<RStore> {
    @Override
    public String typeName() {
        return "store";
    }

    @Override
    public Class<RStore> typeClass() {
        return RStore.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RStore.class);
    }
}
