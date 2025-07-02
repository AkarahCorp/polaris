package dev.akarah.cdata.script.dsl;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.text.Parser;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.flow.AllOfAction;
import dev.akarah.cdata.script.expr.flow.EndAction;
import dev.akarah.cdata.script.expr.flow.GetLocalAction;
import dev.akarah.cdata.script.expr.number.NumberExpression;
import dev.akarah.cdata.script.expr.string.StringExpression;
import dev.akarah.cdata.script.expr.text.TextExpression;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class DslParser {
    List<DslToken> tokens;
    int index;

    public static Expression parseTopLevelExpression(List<DslToken> tokens) {
        var parser = new DslParser();
        parser.tokens = tokens;

        var expressions = new ArrayList<Expression>();
        while(true) {
            if(parser.peek() instanceof DslToken.EOF) {
                return new AllOfAction(expressions);
            }
            var expr = parser.parseAnyExpression();
            expressions.add(expr);
        }
    }

    public Expression parseAnyExpression() {
        return getFirst(
                Pair.of("invocation", this::parseInvocation),
                Pair.of("block", this::parseBlock),
                Pair.of("base", this::parseBaseExpression)
        );
    }

    public Expression parseBlock() {
        var statements = new ArrayList<Expression>();
        expect(DslToken.OpenBrace.class);
        while(!(peek() instanceof DslToken.CloseBrace)) {
            statements.add(parseAnyExpression());
        }
        expect(DslToken.CloseBrace.class);
        return new AllOfAction(statements);
    }

    public Expression getFirst(Pair<String, Supplier<Expression>>... expressions) {
        var oldIdx = this.index;
        var exceptions = new ArrayList<>();
        for(var expr : expressions) {
            this.index = oldIdx;
            try {
                return expr.getSecond().get();
            } catch (Exception ignored) {
                exceptions.add(ignored);
            }
        }
        this.index = oldIdx;
        throw new RuntimeException("unable to parse :( " + exceptions.stream().map(x -> "\n- " + x.toString()).toList() + "\n at cursor " + this.index + "\nin " + this.tokens + "\n");
    }

    public Expression parseInvocation() {
        var base = expect(DslToken.Identifier.class);
        expect(DslToken.OpenParen.class);
        var exprs = new ArrayList<Expression>();
        while(!(peek() instanceof DslToken.CloseParen)) {
            exprs.add(parseAnyExpression());
        }
        expect(DslToken.CloseParen.class);

        var exprClass = ExtBuiltInRegistries.ACTION_TYPE
                .get(ResourceLocation.withDefaultNamespace(base.identifier().replace(".", "/")))
                .orElseThrow()
                .value();

        var ctorArguments = switch (exprs.size()) {
            case 0 -> new Class[]{};
            case 1 -> new Class[]{Expression.class};
            case 2 -> new Class[]{Expression.class, Expression.class};
            case 3 -> new Class[]{Expression.class, Expression.class, Expression.class};
            default -> throw new RuntimeException("too many args sorry :(");
        };
        var emptyArguments = switch (exprs.size()) {
            case 0 -> new Expression[]{};
            case 1 -> new Expression[]{exprs.getFirst()};
            case 2 -> new Expression[]{exprs.getFirst(), exprs.get(1)};
            case 3 -> new Expression[]{exprs.getFirst(), exprs.get(1), exprs.get(2)};
            default -> throw new RuntimeException("too many args sorry :(");
        };
        try {
            var ctor = exprClass.getConstructor(ctorArguments);
            var invocation = ctor.newInstance((Object[]) emptyArguments);
            return invocation;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Expression parseBaseExpression() {
        var tok = read();
        return switch (tok) {
            case DslToken.NumberExpr numberExpr -> new NumberExpression(numberExpr.value());
            case DslToken.StringExpr stringExpr -> new StringExpression(stringExpr.value());
            case DslToken.TextExpr textExpr -> new TextExpression(Parser.parseTextLine(textExpr.value()));
            case DslToken.Identifier identifier -> new GetLocalAction(identifier.identifier());
            default -> throw new IllegalStateException("Unexpected value: " + tok);
        };
    }

    public DslToken peek() {
        return this.tokens.get(this.index);
    }

    public DslToken read() {
        return this.tokens.get(this.index++);
    }

    public <T extends DslToken> T expect(Class<T> clazz) {
        var token = read();
        if(clazz.isInstance(token)) {
            return clazz.cast(token);
        } else {
            throw new RuntimeException("expected " + clazz.getSimpleName() + ", found " + token.getClass().getSimpleName());
        }
    }
}
