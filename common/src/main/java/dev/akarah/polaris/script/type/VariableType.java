package dev.akarah.polaris.script.type;


import dev.akarah.polaris.script.params.ExpressionTypeSet;

import java.lang.constant.ClassDesc;

public record VariableType(
        ExpressionTypeSet typeSet,
        String variableName
) implements Type<Object> {
    @Override
    public String typeName() {
        if(typeSet == null) {
            return this.variableName;
        }
        var rt = this.typeSet.resolveTypeVariable(this.variableName);
        if(rt == null) {
            return this.variableName;
        } else {
            return rt.typeName();
        }
    }

    @Override
    public String verboseTypeName() {
        if(typeSet == null) {
            return this.variableName;
        }

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
