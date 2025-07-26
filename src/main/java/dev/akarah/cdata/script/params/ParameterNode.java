package dev.akarah.cdata.script.params;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

public interface ParameterNode {
    String name();
    Type<?> typePattern();
    Expression makeTypeSafeExpression(CodegenContext ctx, ExpressionStream expressionStream, ExpressionTypeSet typeSet);
}
