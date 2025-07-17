package dev.akarah.cdata.script.params;

import com.google.common.collect.Maps;

import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.nodes.OptionalParameter;
import dev.akarah.cdata.script.params.nodes.RequiredParameter;
import dev.akarah.cdata.script.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Lists;

public class ExpressionTypeSet {
    List<ParameterNode> parameters;
    Type<?> returnType;
    Map<String, Type<?>> typeVariables = Maps.newHashMap();

    public static Builder builder() {
        return new Builder();
    }

    public List<Expression> typecheck(CodegenContext ctx, ExpressionStream stream) {
        this.typeVariables.clear();
        var output = new ArrayList<Expression>();
        for(var parameter : this.parameters) {
            output.add(parameter.makeTypeSafeExpression(ctx, stream, this));
        }
        return output;
    }

    public Type<?> returns() {
        return this.returnType;
    }

    public Type<?> resolveTypeVariable(String variableName) {
        if(!this.typeVariables.containsKey(variableName)) {
            return null;
        }
        return this.typeVariables.get(variableName);
    }

    public void resolveTypeVariable(String variableName, Type<?> hint, SpanData fallbackSpan) {
        if(!this.typeVariables.containsKey(variableName)) {
            this.typeVariables.put(variableName, hint);
        } else if(this.typeVariables.containsKey(variableName) && !this.typeVariables.get(variableName).typeEquals(hint)) {
            throw new ParsingException(
                    "Expected value of type `"
                            + this.typeVariables.get(variableName).verboseTypeName()
                            + "`, got value of type `"
                            + hint.verboseTypeName()
                            + "`",
                    fallbackSpan
            );
        }
    }

    public static class Builder {
        List<Function<ExpressionTypeSet, ParameterNode>> parameters = Lists.newArrayList();
        Function<ExpressionTypeSet, Type<?>> returnType = _ -> Type.void_();

        public ExpressionTypeSet build() {
            var s = new ExpressionTypeSet();
            s.parameters = this.parameters
                    .stream()
                    .map(x -> x.apply(s))
                    .toList();
            s.returnType = this.returnType.apply(s);
            return s;
        }

        public Builder required(String name, Type<?> type) {
            this.parameters.add(_ -> new RequiredParameter(name, type));
            return this;
        }

        public Builder required(String name, Function<ExpressionTypeSet, Type<?>> type) {
            this.parameters.add(e -> new RequiredParameter(name, type.apply(e)));
            return this;
        }

        public Builder optional(String name, Type<?> type) {
            this.parameters.add(_ -> new OptionalParameter(name, type));
            return this;
        }

        public Builder optional(String name, Function<ExpressionTypeSet, Type<?>> type) {
            this.parameters.add(e -> new OptionalParameter(name, type.apply(e)));
            return this;
        }

        public Builder returns(Type<?> type) {
            this.returnType = _ -> type;
            return this;
        }

        public Builder returns(Function<ExpressionTypeSet, Type<?>> type) {
            this.returnType = type;
            return this;
        }
    }
}
