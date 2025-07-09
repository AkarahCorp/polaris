package dev.akarah.cdata.script.params;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;

public interface ParameterNode {
    String name();
    Expression makeTypeSafeExpression(CodegenContext ctx, ExpressionStream expressionStream, ExpressionTypeSet typeSet);
}
