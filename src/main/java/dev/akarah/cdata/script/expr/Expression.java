package dev.akarah.cdata.script.expr;

import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.ast.AllOfAction;
import dev.akarah.cdata.script.expr.ast.IfAction;
import dev.akarah.cdata.script.expr.ast.RepeatTimesAction;
import dev.akarah.cdata.script.expr.ast.ReturnAction;
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

    default Expression flatten() {
        if(this instanceof SpannedExpression<?> expression) {
            return expression.expression();
        }
        return this;
    }

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
        System.out.println("[Internal Warning, please report this if you see this!] " + this + " has no span data.");
        return new SpanData(0, 0, "unknown", ResourceLocation.withDefaultNamespace("error/unspanned"));
    }

    default boolean validateReturnOnAllBranches(CodegenContext ctx, Type<?> type) {
        return switch (this.flatten()) {
            case AllOfAction allOfAction -> {
                for(var action : allOfAction.actions()) {
                    if(action.flatten() instanceof ReturnAction) {
                        yield action.flatten().validateReturnOnAllBranches(ctx, type);
                    }
                }
                yield false;
            }
            case ReturnAction returnAction -> {
                var foundType = ctx.getTypeOf(returnAction.value());
                if(foundType.typeEquals(type)) {
                    yield true;
                } else {
                    throw new ParsingException(
                            "Expected type " + type + ", found type " + foundType,
                            returnAction.value().span()
                    );
                }
            }
            case IfAction ifAction -> {
                yield ifAction.then().validateReturnOnAllBranches(ctx, type)
                        && ifAction.orElse().map(x -> x.validateReturnOnAllBranches(ctx, type)).orElse(true);
            }
            case RepeatTimesAction repeatTimesAction -> repeatTimesAction.perform().validateReturnOnAllBranches(ctx, type);
            default -> false;
        };
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
