package dev.akarah.cdata.script.expr.ast.func;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MethodTypeHint {
    String signature();
    String documentation() default "No documentation written yet.";
}
