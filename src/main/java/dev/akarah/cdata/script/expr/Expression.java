package dev.akarah.cdata.script.expr;

import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.ast.value.StringExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;

public interface Expression {
    void compile(CodegenContext ctx);

    default Type<?> type(CodegenContext ctx) {
        try {
            var lookup = MethodHandles.privateLookupIn(this.getClass(), MethodHandles.lookup());
            return ((ExpressionTypeSet) lookup.findStatic(this.getClass(), "parameters", MethodType.methodType(ExpressionTypeSet.class))
                    .invoke())
                    .returns();
        } catch (Throwable e) {
            throw new ParsingException("[Internal Error, please report!] Default type impl not supported for " + this, this.span());
        }
    }

    default SpanData span() {
        return new SpanData(0, 0, "unknown", ResourceLocation.withDefaultNamespace("error/unspanned"));
    }

    static Object bootStrap(Registry<Class<? extends Expression>> actions) {
        var failures = new ArrayList<>();
        actions.listElements().forEach(classReference -> {
            try {
                var lookup = MethodHandles.privateLookupIn(classReference.value(), MethodHandles.lookup());
                lookup.findStatic(classReference.value(), "parameters", MethodType.methodType(ExpressionTypeSet.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                failures.add(classReference.key().location());
            }
        });
        if(!failures.isEmpty()) {
            throw new RuntimeException("The following actions lack the Expression#parameters() -> ExpressionTypeSet, " + failures);
        }

        return StringExpression.class;
    }
}
