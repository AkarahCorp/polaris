package dev.akarah.polaris.script.params;

import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;

public interface ParameterNode {
    String name();
    Type<?> typePattern();
    Expression makeTypeSafeExpression(CodegenContext ctx, ExpressionStream expressionStream, ExpressionTypeSet typeSet);
}
