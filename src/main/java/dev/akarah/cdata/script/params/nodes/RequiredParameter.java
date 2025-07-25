package dev.akarah.cdata.script.params.nodes;

import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionStream;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.params.ParameterNode;
import dev.akarah.cdata.script.type.Type;

public record RequiredParameter(
        String name,
        Type<?> typePattern
) implements ParameterNode {

    @Override
    public Expression makeTypeSafeExpression(CodegenContext ctx, ExpressionStream expressionStream, ExpressionTypeSet typeSet) {
        var expression = expressionStream.peek();
        if(expression == null) {
            throw new ParsingException(
                    "Missing value of type `" + this.typePattern().verboseTypeName()
                            + "` for parameter `" + this.name(),
                    expressionStream.span()
            );
        }
        var exprType = ctx.getTypeOf(expression);
        var newRequiredType = exprType.resolveTypeVariables(this.typePattern, typeSet, expression.span());
        if(!exprType.typeEquals(newRequiredType)) {
            throw new ParsingException(
                    "Expected value of type `" + newRequiredType.verboseTypeName()
                            + "` for parameter `" + this.name()
                            + "`, got value of type `" + exprType.verboseTypeName() + "`",
                    expression.span()
            );
        }

        return expressionStream.read();
    }
}
