package dev.akarah.polaris.script.type;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.exception.ParsingException;
import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RStruct;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Map;

public record UnresolvedUserType(
        Map<ResourceLocation, StructType> userTypes,
        ResourceLocation name,
        SpanData spanData
) implements Type<RStruct> {

    public Type<RStruct> resolve() {
        try {
            return this.userTypes.get(this.name);
        } catch (Exception e) {
            throw new ParsingException("`" + this.name + "` is not a valid type.", this.spanData);
        }
    }
    @Override
    public String typeName() {
        return this.resolve().typeName();
    }

    @Override
    public Class<RStruct> typeClass() {
        return this.resolve().typeClass();
    }

    @Override
    public ClassDesc classDescType() {
        return this.resolve().classDescType();
    }

    @Override
    public String verboseTypeName() {
        return this.resolve().verboseTypeName();
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
