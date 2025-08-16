package dev.akarah.polaris.script.expr.docs;

import dev.akarah.polaris.script.expr.ast.func.ClassDocumentation;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.type.Type;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record Document(
        String prettyName,
        String details,
        String className,
        List<MethodEntry> methods
) {
    public record MethodEntry(
            String name,
            String signature,
            String documentation,
            DocumentationOrdering order
    ) {
        public static MethodEntry fromMethod(Method method) {
            var name = method.getName().replace("__", ".");
            var signature = "";
            var documentation = "";
            var order = DocumentationOrdering.DEFAULT;

            for(var annotation : method.getAnnotations()) {
                if(annotation instanceof MethodTypeHint methodTypeHint) {
                    signature = methodTypeHint.signature();
                    documentation = methodTypeHint.documentation();
                    order = methodTypeHint.order();
                }
            }
            return new MethodEntry(name, signature, documentation, order);
        }

        @Override
        public @NotNull String toString() {
            return "#### `" + this.name + this.signature + "`" +
                    "\n\n" +
                    this.documentation +
                    "\n";
        }
    }

    public static Document fromType(Type<?> type) {
        return fromClass(type.typeClass(), type.typeName());
    }

    public static Document fromClass(Class<?> clazz, String className) {
        var methods = new ArrayList<MethodEntry>();
        var prettyName = className;
        var details = "*No documentation about this class has been written yet.*";
        for(var method : clazz.getDeclaredMethods()) {
            if(Arrays.stream(method.getAnnotations()).anyMatch(x -> x instanceof MethodTypeHint)) {
                methods.add(MethodEntry.fromMethod(method));
            }
        }
        for(var annotation : clazz.getAnnotations()) {
            if(annotation instanceof ClassDocumentation classDocumentation) {
                prettyName = classDocumentation.prettifiedName();
                details = classDocumentation.details();
            }
        }
        methods.sort(new MethodSorter());
        return new Document(prettyName, details, className, methods);
    }



    @Override
    public @NotNull String toString() {
        var sb = new StringBuilder();
        sb.append("# ").append(this.prettyName);
        sb.append("\n");
        sb.append(this.details);
        sb.append("\n\n\n");
        for(var method : this.methods) {
            sb.append(method.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
