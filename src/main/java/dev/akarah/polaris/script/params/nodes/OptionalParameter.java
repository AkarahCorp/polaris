package dev.akarah.polaris.script.params.nodes;

import dev.akarah.polaris.script.exception.ParsingException;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.params.ExpressionStream;
import dev.akarah.polaris.script.params.ExpressionTypeSet;
import dev.akarah.polaris.script.params.ParameterNode;
import dev.akarah.polaris.script.type.Type;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public record OptionalParameter(
        String name,
        Type<?> typePattern
) implements ParameterNode {
    public static Lock lock = new ReentrantLock();

    @Override
    public Expression makeTypeSafeExpression(CodegenContext ctx, ExpressionStream expressionStream, ExpressionTypeSet typeSet) {
        var expression = expressionStream.peek();
        if(expression == null) {
            return null;
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
