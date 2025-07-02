package dev.akarah.cdata.script.dsl;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.text.Parser;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.flow.AllOfAction;
import dev.akarah.cdata.script.expr.flow.GetLocalAction;
import dev.akarah.cdata.script.expr.flow.RepeatTimesAction;
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
            var expr = parser.parseStatement();
            expressions.add(expr);
        }
    }

    public Expression parseStatement() {
        if(this.peek() instanceof DslToken.RepeatKeyword) {
            return parseRepeat();
        }
        return parseValue();
    }

    public Expression parseValue() {
        return this.parseInvocation();
    }

    public RepeatTimesAction parseRepeat() {
        expect(DslToken.RepeatKeyword.class);
        expect(DslToken.OpenParen.class);
        var times = parseValue();
        expect(DslToken.CloseParen.class);
        var block = parseBlock();

        return new RepeatTimesAction(times, block);
    }

    public AllOfAction parseBlock() {
        var statements = new ArrayList<Expression>();
        expect(DslToken.OpenBrace.class);
        while(!(peek() instanceof DslToken.CloseBrace)) {
            statements.add(parseValue());
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
        var baseExpression = parseBaseExpression();

        if(peek() instanceof DslToken.OpenParen
        && baseExpression instanceof GetLocalAction(String functionName)) {
            expect(DslToken.OpenParen.class);

            var parameters = new ArrayList<Expression>();
            while(!(peek() instanceof DslToken.CloseParen)) {
                parameters.add(parseValue());

                if(!(peek() instanceof DslToken.CloseParen)) {
                    expect(DslToken.Comma.class);
                }
            }
            expect(DslToken.CloseParen.class);

            var exprClass = ExtBuiltInRegistries.ACTION_TYPE
                    .get(ResourceLocation.withDefaultNamespace(functionName.replace(".", "/")))
                    .orElseThrow()
                    .value();

            var constructorArguments = repeatInArray(parameters.size());
            var emptyArguments = toArray(parameters);

            try {
                var constructor = exprClass.getConstructor(constructorArguments);
                baseExpression = constructor.newInstance((Object[]) emptyArguments);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
        return baseExpression;
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

    @SuppressWarnings("unchecked")
    private static Expression[] toArray(List<Expression> list) {
        var array = new Expression[list.size()];
        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    private static Class<Expression>[] repeatInArray(int size) {
        var array = new Class[size];
        Arrays.fill(array, Expression.class);
        return array;
    }
}
