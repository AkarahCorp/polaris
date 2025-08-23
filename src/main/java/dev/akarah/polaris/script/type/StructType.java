package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RStruct;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.lang.constant.ClassDesc;
import java.util.List;

public record StructType(ResourceLocation name, List<Field> fields) implements Type<RStruct> {
    public record Field(
            String name,
            Type<?> type,
            Expression fallback
    ) {
        @Override
        public @NotNull String toString() {
            return name + ": " + type.verboseTypeName();
        }
    }

    @Override
    public String typeName() {
        return this.name.toString();
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
        return this.name.toString();
    }
}
