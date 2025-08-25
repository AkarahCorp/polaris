package dev.akarah.polaris.script.expr.ast;

import dev.akarah.polaris.script.expr.Expression;

import java.util.List;

public record Annotation(
        String name,
        List<Expression> parameters
) {
}
