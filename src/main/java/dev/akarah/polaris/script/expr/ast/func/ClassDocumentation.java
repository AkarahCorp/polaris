package dev.akarah.polaris.script.expr.ast.func;

import dev.akarah.polaris.script.expr.docs.DocumentationOrdering;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ClassDocumentation {
    String prettifiedName();
    String details() default "*No documentation about this class has been written yet.*";
}
