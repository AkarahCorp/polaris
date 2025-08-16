package dev.akarah.polaris.script.expr.docs;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.type.Type;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record Document(
        String className,
        List<MethodEntry> methods
) {
    record MethodEntry(
            String name,
            String signature,
            String documentation
    ) {
        public static MethodEntry fromMethod(Method method) {
            var name = method.getName();
            var signature = "";
            var documentation = "";
            for(var annotation : method.getAnnotations()) {
                if(annotation instanceof MethodTypeHint methodTypeHint) {
                    signature = methodTypeHint.signature();
                    documentation = methodTypeHint.documentation();
                }
            }
            return new MethodEntry(name, signature, documentation);
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
        for(var method : clazz.getDeclaredMethods()) {
            if(Arrays.stream(method.getAnnotations()).anyMatch(x -> x instanceof MethodTypeHint)) {
                methods.add(MethodEntry.fromMethod(method));
            }
        }
        return new Document(className, methods);
    }



    @Override
    public @NotNull String toString() {
        var sb = new StringBuilder();
        sb.append("# ").append(this.className);
        sb.append("\n");
        for(var method : this.methods) {
            sb.append(method.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
