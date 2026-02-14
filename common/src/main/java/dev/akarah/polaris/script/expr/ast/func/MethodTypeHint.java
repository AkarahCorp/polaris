package dev.akarah.polaris.script.expr.ast.func;

import dev.akarah.polaris.script.expr.docs.DocumentationOrdering;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MethodTypeHint {
    String signature();
    String documentation() default "*No documentation about this function has been written yet.*";
    DocumentationOrdering order() default DocumentationOrdering.DEFAULT;
}
