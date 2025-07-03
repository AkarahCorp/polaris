package dev.akarah.cdata.script.exception;

import dev.akarah.cdata.script.expr.Expression;

public class TypeCheckException extends SpannedException {
    Expression relevantExpression;

    public TypeCheckException(String message, Expression expression) {
        super(message, expression.span());
        this.relevantExpression = expression;
    }
}
