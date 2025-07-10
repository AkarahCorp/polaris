package dev.akarah.cdata.script.type;

import dev.akarah.cdata.db.DataStore;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import net.minecraft.network.chat.Component;

import java.lang.constant.ClassDesc;

public record StoreType() implements Type<DataStore> {
    @Override
    public String typeName() {
        return "store";
    }

    @Override
    public Class<DataStore> typeClass() {
        return DataStore.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(DataStore.class);
    }
}
