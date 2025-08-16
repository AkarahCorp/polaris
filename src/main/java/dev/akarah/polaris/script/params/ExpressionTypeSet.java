package dev.akarah.polaris.script.params;

import com.google.common.collect.Maps;

import dev.akarah.polaris.script.exception.ParsingException;
import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.params.nodes.OptionalParameter;
import dev.akarah.polaris.script.params.nodes.RequiredParameter;
import dev.akarah.polaris.script.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Lists;

public class ExpressionTypeSet {
    List<ParameterNode> parameters;
    Type<?> returnType;
    Map<String, Type<?>> typeVariables = Maps.newHashMap();
    String functionName;

    public static Builder builder(String functionName) {
        return new Builder(functionName);
    }

    public List<Expression> typecheck(CodegenContext ctx, ExpressionStream stream) {
        this.typeVariables.clear();
        var output = new ArrayList<Expression>();
        for(var parameter : this.parameters) {
            output.add(parameter.makeTypeSafeExpression(ctx, stream, this));
        }
        if(stream.peek() != null) {
            throw new ParsingException("Too many arguments!", stream.peek().span());
        }
        return output;
    }

    public List<ParameterNode> parameters() {
        return this.parameters;
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
                            + "` for function `"
                            + this.functionName + "`",
                    fallbackSpan
            );
        }
    }

    public static class Builder {
        List<Function<ExpressionTypeSet, ParameterNode>> parameters = Lists.newArrayList();
        Function<ExpressionTypeSet, Type<?>> returnType = _ -> Type.void_();
        String functionName;

        public Builder(String functionName) {
            this.functionName = functionName;
        }

        public ExpressionTypeSet build() {
            var s = new ExpressionTypeSet();
            s.parameters = this.parameters
                    .stream()
                    .map(x -> x.apply(s))
                    .toList();
            s.returnType = this.returnType.apply(s);
            s.functionName = this.functionName;
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
