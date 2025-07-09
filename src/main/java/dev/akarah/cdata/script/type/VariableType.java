package dev.akarah.cdata.script.type;


import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import net.minecraft.Optionull;

import java.lang.constant.ClassDesc;

public record VariableType(
        ExpressionTypeSet typeSet,
        String variableName
) implements Type<Object> {
    @Override
    public String typeName() {
        var rt = this.typeSet.resolveTypeVariable(this.variableName);
        if(rt == null) {
            return this.variableName;
        } else {
            return rt.typeName();
        }
    }

    @Override
    public String verboseTypeName() {
        var rt = this.typeSet.resolveTypeVariable(this.variableName);
        if(rt == null) {
            return this.variableName;
        } else {
            return rt.verboseTypeName();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Object> typeClass() {
        return (Class<Object>) this.typeSet.resolveTypeVariable(this.variableName).typeClass();
    }

    @Override
    public ClassDesc classDescType() {
        return this.typeSet.resolveTypeVariable(this.variableName).classDescType();
    }
}
