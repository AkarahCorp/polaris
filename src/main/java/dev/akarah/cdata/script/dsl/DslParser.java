package dev.akarah.cdata.script.dsl;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.text.Parser;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.bool.BooleanExpression;
import dev.akarah.cdata.script.expr.flow.*;
import dev.akarah.cdata.script.expr.number.NumberExpression;
import dev.akarah.cdata.script.expr.string.StringExpression;
import dev.akarah.cdata.script.expr.text.TextExpression;
import dev.akarah.cdata.script.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DslParser {
    List<DslToken> tokens;
    int index;

    public static SchemaExpression parseTopLevelExpression(List<DslToken> tokens) {
        var parser = new DslParser();
        parser.tokens = tokens;

        return parser.parseSchema();
    }

    public SchemaExpression parseSchema() {
        expect(DslToken.SchemaKeyword.class);
        expect(DslToken.OpenParen.class);

        var parameters = new ArrayList<Pair<String, Type<?>>>();
        while(!(peek() instanceof DslToken.CloseParen)) {
            var name = expect(DslToken.Identifier.class).identifier();
            var type = parseType();
            parameters.add(Pair.of(name, type));
            if(!(peek() instanceof DslToken.CloseParen)) {
                expect(DslToken.Comma.class);
            }
        }
        expect(DslToken.CloseParen.class);

        Type<?> returnType = Type.void_();
        if(peek() instanceof DslToken.ArrowSymbol) {
            expect(DslToken.ArrowSymbol.class);
            returnType = parseType();
        }

        var body = parseBlock();
        return new SchemaExpression(parameters, returnType, body);
    }

    public Type<?> parseType() {
        var identifier = expect(DslToken.Identifier.class).identifier();
        return switch (identifier) {
            case "any" -> Type.any();
            case "void" -> Type.void_();
            case "number" -> Type.number();
            case "bool" -> Type.bool();
            case "string" -> Type.string();
            case "vec3" -> Type.vec3();
            case "text" -> Type.text();
            case "list" -> Type.list();
            default -> throw new RuntimeException("Type `" + identifier + "` is unknown.");
        };
    }

    public Expression parseStatement() {
        if(this.peek() instanceof DslToken.RepeatKeyword) {
            return parseRepeat();
        }
        if(this.peek() instanceof DslToken.IfKeyword) {
            return parseIf();
        }
        return parseValue();
    }

    public Expression parseValue() {
        return this.parseStorage();
    }

    public RepeatTimesAction parseRepeat() {
        expect(DslToken.RepeatKeyword.class);
        expect(DslToken.OpenParen.class);
        var times = parseValue();
        expect(DslToken.CloseParen.class);
        var block = parseBlock();

        return new RepeatTimesAction(times, block);
    }

    public IfAction parseIf() {
        expect(DslToken.IfKeyword.class);
        expect(DslToken.OpenParen.class);
        var times = parseValue();
        expect(DslToken.CloseParen.class);
        var block = parseBlock();

        var orElse = Optional.<Expression>empty();
        if(peek() instanceof DslToken.ElseKeyword) {
            expect(DslToken.ElseKeyword.class);
            orElse = Optional.of(parseBlock());
        }

        return new IfAction(times, block, orElse);
    }

    public AllOfAction parseBlock() {
        var statements = new ArrayList<Expression>();
        expect(DslToken.OpenBrace.class);
        while(!(peek() instanceof DslToken.CloseBrace)) {
            statements.add(parseStatement());
        }
        expect(DslToken.CloseBrace.class);
        return new AllOfAction(statements);
    }

    public Expression parseStorage() {
        var baseExpression = parseArrowExpression();
        while(peek() instanceof DslToken.EqualSymbol
        && baseExpression instanceof GetLocalAction(String variable)) {
            expect(DslToken.EqualSymbol.class);
            baseExpression = new SetLocalAction(variable, parseValue());
        }
        return baseExpression;
    }

    public Expression parseArrowExpression() {
        var baseExpression = parseInvocation();
        while(peek() instanceof DslToken.ArrowSymbol) {
            expect(DslToken.ArrowSymbol.class);
            var name = expect(DslToken.Identifier.class).identifier();
            var parameters = parseTuple();
            parameters.addFirst(baseExpression);
            baseExpression = new LateResolvedFunctionCall(name, parameters);
        }
        return baseExpression;
    }

    public Expression parseInvocation() {
        var baseExpression = parseBaseExpression();

        if(peek() instanceof DslToken.OpenParen && baseExpression instanceof GetLocalAction(String functionName)) {
            var tuple = parseTuple();
            baseExpression = new LateResolvedFunctionCall(functionName, tuple);
        }
        return baseExpression;
    }

    public ArrayList<Expression> parseTuple() {
        expect(DslToken.OpenParen.class);
        var parameters = new ArrayList<Expression>();
        while(!(peek() instanceof DslToken.CloseParen)) {
            parameters.add(parseValue());

            if(!(peek() instanceof DslToken.CloseParen)) {
                expect(DslToken.Comma.class);
            }
        }
        expect(DslToken.CloseParen.class);
        return parameters;
    }

    public Expression parseBaseExpression() {
        var tok = read();
        return switch (tok) {
            case DslToken.NumberExpr numberExpr -> new NumberExpression(numberExpr.value());
            case DslToken.StringExpr stringExpr -> new StringExpression(stringExpr.value());
            case DslToken.TextExpr textExpr -> new TextExpression(Parser.parseTextLine(textExpr.value()));
            case DslToken.Identifier(String id) when id.equals("true") -> new BooleanExpression(true);
            case DslToken.Identifier(String id) when id.equals("false") -> new BooleanExpression(false);
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
