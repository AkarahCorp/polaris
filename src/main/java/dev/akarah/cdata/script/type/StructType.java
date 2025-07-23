package dev.akarah.cdata.script.type;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RDict;
import dev.akarah.cdata.script.value.RStruct;
import org.jetbrains.annotations.NotNull;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Map;

public record StructType(String name, List<Field> fields) implements Type<RStruct> {
    public record Field(
            String name,
            Type<?> type
    ) {
        @Override
        public @NotNull String toString() {
            return name + ": " + type.verboseTypeName();
        }
    }

    @Override
    public String typeName() {
        return this.name;
    }

    @Override
    public Class<RStruct> typeClass() {
        return RStruct.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RStruct.class);
    }

    @Override
    public String verboseTypeName() {
        return this.name;
    }
}
