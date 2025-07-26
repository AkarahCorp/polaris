package dev.akarah.cdata.script.dsl;

import java.lang.constant.MethodTypeDesc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.SpannedExpression;
import dev.akarah.cdata.script.expr.ast.*;
import dev.akarah.cdata.script.expr.ast.func.LateResolvedFunctionCall;
import dev.akarah.cdata.script.expr.ast.value.*;
import dev.akarah.cdata.script.expr.ast.operation.*;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.SpannedType;
import dev.akarah.cdata.script.type.StatsObjectType;
import dev.akarah.cdata.script.type.StructType;
import dev.akarah.cdata.script.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import dev.akarah.cdata.script.expr.ast.func.JvmFunctionAction;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RBoolean;
import net.minecraft.client.renderer.texture.Stitcher;

public class DslParser {
    List<DslToken> tokens;
    Map<String, StructType> userTypes = Maps.newHashMap();
    int index;

    public static Expression parseTopLevelExpression(List<DslToken> tokens, Map<String, StructType> userTypes) {
        var parser = new DslParser();
        parser.tokens = tokens;
        parser.userTypes = userTypes;

        if(parser.peek() instanceof DslToken.StructKeyword) {
            var outputType = (StructType) parser.parseType().flatten();
            return new TypeExpression(outputType);
        }
        if(parser.peek() instanceof DslToken.EventKeyword) {
            return parser.parseEvent();
        }
        return parser.parseSchema();
    }

    public static ExpressionTypeSet parseExpressionTypeSet(List<DslToken> tokens, String functionName) {
        var parser = new DslParser();
        parser.tokens = tokens;

        return parser.parseTypeSet(functionName);
    }

    public static Type<?> parseTopLevelType(List<DslToken> tokens) {
        var parser = new DslParser();
        parser.tokens = tokens;

        return parser.parseType();
    }

    public SchemaExpression parseSchema() {
        var kw = expect(DslToken.FunctionKeyword.class);
        var typeSet = parseTypeSet("user_func");
        var body = parseBlock();
        return new SchemaExpression(typeSet, body, Optional.empty(), kw.span());
    }

    // TODO: merge with parseSchema
    public SchemaExpression parseEvent() {
        var kw = expect(DslToken.EventKeyword.class);
        var eventName = expect(DslToken.Identifier.class);
        var typeSet = parseTypeSet(eventName.identifier());
        var body = parseBlock();
        return new SchemaExpression(typeSet, body, Optional.of(eventName.identifier()), kw.span());
    }

    public Type<?> parseType() {
        return parseType(List.of()).apply(null);
    }

    public Function<ExpressionTypeSet, Type<?>> parseType(List<String> typeVariables) {
        return switch (peek()) {
            case DslToken.FunctionKeyword _ -> {
                expect(DslToken.FunctionKeyword.class);
                var parameterTypes = Lists.<Function<ExpressionTypeSet, Type<?>>>newArrayList();
                expect(DslToken.OpenParen.class);
                while(!(peek() instanceof DslToken.CloseParen)) {
                    parameterTypes.add(parseType(typeVariables));
                    if(!(peek() instanceof DslToken.CloseParen)) {
                        expect(DslToken.Comma.class);
                    }
                }
                expect(DslToken.CloseParen.class);

                expect(DslToken.ArrowSymbol.class);
                var returnType = parseType(typeVariables);
                yield e -> Type.function(
                        returnType.apply(e),
                        parameterTypes
                                .stream()
                                .map(x -> x.apply(e))
                                .toList()
                );
            }
            case DslToken.StructKeyword structKeyword -> e -> {
                expect(DslToken.StructKeyword.class);
                expect(DslToken.OpenBrace.class);
                var fields = Lists.<StructType.Field>newArrayList();
                while(!(peek() instanceof DslToken.CloseBrace)) {
                    var name = expect(DslToken.Identifier.class);
                    expect(DslToken.Colon.class);
                    var type = parseType(typeVariables);
                    Expression fallback = null;
                    if(peek() instanceof DslToken.EqualSymbol equalSymbol) {
                        expect(DslToken.EqualSymbol.class);
                        fallback = parseBaseExpression();
                    }
                    fields.add(new StructType.Field(name.identifier(), type.apply(e), fallback));
                    if(!(peek() instanceof DslToken.CloseBrace)) {
                        expect(DslToken.Comma.class);
                    }
                }
                expect(DslToken.CloseBrace.class);
                return new SpannedType<>(
                        Type.struct(
                                structKeyword.span().fileName().toString()
                                        .replace(":", ".")
                                        .replace("/", "."),
                                fields
                        ),
                        structKeyword.span()
                );
            };
            default -> {
                var identifier = expect(DslToken.Identifier.class);
                if(typeVariables.contains(identifier.identifier())) {
                    yield e -> Type.var(e, identifier.identifier());
                }
                yield switch (identifier.identifier()) {
                    case "any" -> _ -> new SpannedType<>(Type.any(), identifier.span());
                    case "void" -> _ -> new SpannedType<>(Type.void_(), identifier.span());
                    case "number" -> _ -> new SpannedType<>(Type.number(), identifier.span());
                    case "boolean" -> _ -> new SpannedType<>(Type.bool(), identifier.span());
                    case "string" -> _ -> new SpannedType<>(Type.string(), identifier.span());
                    case "vector" -> _ -> new SpannedType<>(Type.vector(), identifier.span());
                    case "text" -> _ -> new SpannedType<>(Type.text(), identifier.span());
                    case "inventory" -> _ -> new SpannedType<>(Type.inventory(), identifier.span());
                    case "store" -> _ -> new SpannedType<>(Type.store(), identifier.span());
                    case "world" -> _ -> new SpannedType<>(Type.world(), identifier.span());
                    case "uuid" -> _ -> new SpannedType<>(Type.uuid(), identifier.span());
                    case "stat_obj" -> _ -> new SpannedType<>(new StatsObjectType(), identifier.span());
                    case "nullable" -> {
                        expect(DslToken.OpenBracket.class);
                        var subtype = parseType(typeVariables);
                        expect(DslToken.CloseBracket.class);
                        yield e -> new SpannedType<>(Type.nullable(subtype.apply(e)), identifier.span());
                    }
                    case "list" -> {
                        expect(DslToken.OpenBracket.class);
                        var subtype = parseType(typeVariables);
                        expect(DslToken.CloseBracket.class);
                        yield e -> new SpannedType<>(Type.list(subtype.apply(e)), identifier.span());
                    }
                    case "dict" -> {
                        expect(DslToken.OpenBracket.class);
                        var keyType = parseType(typeVariables);
                        expect(DslToken.Comma.class);
                        var valueType = parseType(typeVariables);
                        expect(DslToken.CloseBracket.class);
                        yield e -> new SpannedType<>(Type.dict(keyType.apply(e), valueType.apply(e)), identifier.span());
                    }
                    case "entity" -> _ -> new SpannedType<>(Type.entity(), identifier.span());
                    case "item" -> _ -> new SpannedType<>(Type.itemStack(), identifier.span());
                    case "identifier" -> _ -> new SpannedType<>(Type.identifier(), identifier.span());
                    default -> _ -> {
                        if(this.userTypes.containsKey(identifier.identifier().replace(".", "_"))) {
                            return new SpannedType<>(
                                    this.userTypes.get(identifier.identifier().replace(".", "_")),
                                    identifier.span()
                            );
                        }
                        throw new ParsingException("`" + identifier.identifier() + "` is not a valid type.", identifier.span());
                    };
                };
            }
        };
    }

    public ExpressionTypeSet parseTypeSet(String functionName) {
        var ts = ExpressionTypeSet.builder(functionName);

        var typeParameters = new ArrayList<String>();
        if(peek() instanceof DslToken.LessThanSymbol) {
            expect(DslToken.LessThanSymbol.class);
            while(!(peek() instanceof DslToken.GreaterThanSymbol)) {
                typeParameters.add(expect(DslToken.Identifier.class).identifier());
                if(!(peek() instanceof DslToken.GreaterThanSymbol)) {
                    expect(DslToken.Comma.class);
                }
            }
            expect(DslToken.GreaterThanSymbol.class);
        }


        expect(DslToken.OpenParen.class);
        while(!(peek() instanceof DslToken.CloseParen)) {
            var name = expect(DslToken.Identifier.class);

            var required = true;
            if(peek() instanceof DslToken.QuestionMark) {
                required = false;
                expect(DslToken.QuestionMark.class);
            }
            expect(DslToken.Colon.class);
            var type = parseType(typeParameters);


            if(required) {
                ts.required(name.identifier(), type);
            } else {
                ts.optional(name.identifier(), type);
            }

            if(!(peek() instanceof DslToken.CloseParen)) {
                expect(DslToken.Comma.class);
            }
        }
        expect(DslToken.CloseParen.class);

        expect(DslToken.ArrowSymbol.class);
        ts.returns(parseType(typeParameters));

        return ts.build();
    }

    public Expression parseStatement() {
        if(this.peek() instanceof DslToken.RepeatKeyword) {
            return parseRepeat();
        }
        if(this.peek() instanceof DslToken.IfKeyword) {
            return parseIf();
        }
        if(this.peek() instanceof DslToken.ForeachKeyword) {
            return parseForEach();
        }
        if(this.peek() instanceof DslToken.BreakKeyword) {
            expect(DslToken.BreakKeyword.class);
            return new BreakAction();
        }
        if(this.peek() instanceof DslToken.ContinueKeyword) {
            expect(DslToken.ContinueKeyword.class);
            return new ContinueAction();
        }
        if(this.peek() instanceof DslToken.ReturnKeyword) {
            this.expect(DslToken.ReturnKeyword.class);
            if(peek() instanceof DslToken.CloseBrace) {
                return new ReturnAction(null);
            }
            return new ReturnAction(parseValue());
        }
        return parseStorage();
    }

    public ForEachAction parseForEach() {
        var kw = expect(DslToken.ForeachKeyword.class);
        var variableName = expect(DslToken.Identifier.class);
        expect(DslToken.InKeyword.class);
        var listExpr = parseValue();
        var block = parseBlock();
        return new ForEachAction(listExpr, variableName.identifier(), block, kw.span());
    }

    public Expression parseValue() {
        return this.parseBooleanOperands();
    }

    public Expression parseRepeat() {
        var kw = expect(DslToken.RepeatKeyword.class);
        var times = parseValue();
        var block = parseBlock();

        return new SpannedExpression<>(new RepeatTimesAction(times, block), kw.span());
    }

    public Expression parseIf() {
        var kw = expect(DslToken.IfKeyword.class);
        var times = parseValue();
        var block = parseBlock();

        var orElse = Optional.<Expression>empty();
        if(peek() instanceof DslToken.ElseKeyword) {
            expect(DslToken.ElseKeyword.class);
            orElse = Optional.of(parseBlock());
        }

        return new SpannedExpression<>(new IfAction(times, block, orElse), kw.span());
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
        var baseExpression = parseBooleanOperands();
        var typeHint = Optional.<Type<?>>empty();
        if(peek() instanceof DslToken.Colon) {
            expect(DslToken.Colon.class);
            typeHint = Optional.of(parseType());
        }
        while(peek() instanceof DslToken.EqualSymbol
        && baseExpression instanceof GetLocalAction(String variable, SpanData _)) {
            var eq = expect(DslToken.EqualSymbol.class);
            baseExpression = new SpannedExpression<>(
                    new SetLocalAction(variable, typeHint, parseValue(), eq.span()),
                    eq.span()
            );
        }
        return baseExpression;
    }

    public Expression parseBooleanOperands() {
        var base = parseEqualityExpression();
        while(true) {
            if(peek() instanceof DslToken.DoubleAmpersand) {
                expect(DslToken.DoubleAmpersand.class);
                base = new AndExpression(base, parseEqualityExpression());
            } else if(peek() instanceof DslToken.DoubleLine) {
                expect(DslToken.DoubleLine.class);
                base = new OrExpression(base, parseEqualityExpression());
            } else {
                break;
            }
        }
        return base;
    }

    public Expression parseEqualityExpression() {
        var baseExpression = parseComparisonExpression();
        while(peek() instanceof DslToken.DoubleEqualSymbol) {
            expect(DslToken.DoubleEqualSymbol.class);
            baseExpression = new EqualToExpression(baseExpression, parseEqualityExpression());
        }
        return baseExpression;
    }

    public Expression parseComparisonExpression() {
        var baseExpression = parseCast();
        while(true) {
            if(peek() instanceof DslToken.GreaterThanSymbol) {
                expect(DslToken.GreaterThanSymbol.class);
                if(peek() instanceof DslToken.EqualSymbol) {
                    expect(DslToken.EqualSymbol.class);
                    throw new RuntimeException("TODO");
                } else {
                    baseExpression = new GreaterThanExpression(baseExpression, parseComparisonExpression());
                }
            } else if(peek() instanceof DslToken.LessThanSymbol) {
                expect(DslToken.LessThanSymbol.class);
                if(peek() instanceof DslToken.EqualSymbol) {
                    expect(DslToken.EqualSymbol.class);
                    throw new RuntimeException("TODO");
                } else {
                    baseExpression = new LessThanExpression(baseExpression, parseComparisonExpression());
                }
            } else {
                break;
            }
        }
        return baseExpression;
    }

    public Expression parseCast() {
        var base = parseTerm();
        while(true) {
            if(peek() instanceof DslToken.AsKeyword asKeyword) {
                expect(DslToken.AsKeyword.class);
                base = new CastExpression(base, parseType());
            } else {
                break;
            }
        }
        return base;
    }

    public Expression parseTerm() {
        var base = parseFactor();
        while(true) {
            if(peek() instanceof DslToken.PlusSymbol) {
                var symbol = expect(DslToken.PlusSymbol.class);
                base = new LateResolvedFunctionCall(
                        "add",
                        List.of(base, parseFactor()),
                        symbol.span()
                );
            } else if(peek() instanceof DslToken.StarSymbol) {
                var symbol = expect(DslToken.StarSymbol.class);
                base = new LateResolvedFunctionCall(
                        "mul",
                        List.of(base, parseFactor()),
                        symbol.span()
                );
            } else {
                break;
            }
        }

        return base;
    }

    public Expression parseFactor() {
        var base = parseArrowExpression();
        while(true) {
            if(peek() instanceof DslToken.MinusSymbol) {
                var symbol = expect(DslToken.MinusSymbol.class);
                base = new LateResolvedFunctionCall(
                        "sub",
                        List.of(base, parseInvocation()),
                        symbol.span()
                );
            } else if(peek() instanceof DslToken.SlashSymbol) {
                var symbol = expect(DslToken.SlashSymbol.class);
                base = new LateResolvedFunctionCall(
                        "div",
                        List.of(base, parseInvocation()),
                        symbol.span()
                );
            } else if(peek() instanceof DslToken.Percent) {
                var symbol = expect(DslToken.Percent.class);
                base = new LateResolvedFunctionCall(
                        "rem",
                        List.of(base, parseInvocation()),
                        symbol.span()
                );
            } else {
                break;
            }
        }
        return base;
    }



    public Expression parseArrowExpression() {
        var baseExpression = parseInvocation();
        while(peek() instanceof DslToken.ArrowSymbol) {
            expect(DslToken.ArrowSymbol.class);
            var name = expect(DslToken.Identifier.class);
            var parameters = Lists.<Expression>newArrayList();
            if(peek() instanceof DslToken.OpenParen) {
                parameters.addAll(parseTuple());
            }
            parameters.addFirst(baseExpression);
            baseExpression = new LateResolvedFunctionCall(name.identifier(), parameters, name.span());
        }
        return baseExpression;
    }

    public Expression parseInvocation() {
        var baseExpression = parseNegation();

        if(peek() instanceof DslToken.OpenParen && baseExpression instanceof GetLocalAction(String functionName, SpanData _)) {
            var tuple = parseTuple();
            baseExpression = new LateResolvedFunctionCall(functionName, tuple, baseExpression.span());
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

    public Expression parseNegation() {
        boolean negate = false;
        if(peek() instanceof DslToken.MinusSymbol) {
            expect(DslToken.MinusSymbol.class);
            negate = true;
        }
        boolean boolNot = false;
        if(peek() instanceof DslToken.ExclamationMark) {
            expect(DslToken.ExclamationMark.class);
            boolNot = true;
        }
        var baseExpr = parseBaseExpression();
        if(negate) {
            baseExpr = new LateResolvedFunctionCall(
                    "mul",
                    List.of(baseExpr, new NumberExpression(-1)),
                    baseExpr.span()
            );
        }
        if(boolNot) {
            baseExpr = new JvmFunctionAction(
                CodegenUtil.ofClass(RBoolean.class), 
                "not", 
                MethodTypeDesc.of(
                    CodegenUtil.ofClass(RBoolean.class),
                    List.of(CodegenUtil.ofClass(RBoolean.class))
                ), 
                List.of(baseExpr), 
                Type.bool()
            );
        }
        return baseExpr;
    }

    public Expression parseBaseExpression() {
        var tok = read();
        return switch (tok) {
            case DslToken.FunctionKeyword _ -> {
                this.index -= 1;
                yield this.parseSchema().asLambdaExpression();
            }
            case DslToken.NewKeyword kw -> {
                var name = expect(DslToken.Identifier.class);
                var openBrace = expect(DslToken.OpenBrace.class);
                var map = Lists.<Pair<String, Expression>>newArrayList();
                while(!(peek() instanceof DslToken.CloseBrace)) {
                    var key = expect(DslToken.Identifier.class).identifier();
                    expect(DslToken.EqualSymbol.class);
                    var value = parseValue();
                    if(!(peek() instanceof DslToken.CloseBrace)) {
                        expect(DslToken.Comma.class);
                    }
                    map.add(Pair.of(key, value));
                }
                var closeBrace = expect(DslToken.CloseBrace.class);

                yield new InlineStructExpression(name.identifier(), map, SpanData.merge(openBrace.span(), closeBrace.span()));
            }
            case DslToken.NumberExpr numberExpr -> new SpannedExpression<>(new NumberExpression(numberExpr.value()), numberExpr.span());
            case DslToken.StringExpr stringExpr -> new SpannedExpression<>(new StringExpression(stringExpr.value()), stringExpr.span());
            case DslToken.Identifier(String id, SpanData span) when id.equals("true") ->
                    new SpannedExpression<>(new BooleanExpression(true), span);
            case DslToken.Identifier(String id, SpanData span) when id.equals("false") ->
                    new SpannedExpression<>(new BooleanExpression(false), span);
            case DslToken.Identifier identifier -> new GetLocalAction(identifier.identifier(), identifier.span());
            case DslToken.TextExpr text -> new SpannedExpression<>(new ComponentLiteralExpression(text.value()), text.span());
            case DslToken.OpenBracket openBracket -> {
                var list = new ArrayList<Expression>();
                while(!(peek() instanceof DslToken.CloseBracket)) {
                    list.add(parseValue());
                    if(!(peek() instanceof DslToken.CloseBracket)) {
                        expect(DslToken.Comma.class);
                    }
                }
                var closeBracket = expect(DslToken.CloseBracket.class);
                yield new InlineListExpression(list, SpanData.merge(openBracket.span(), closeBracket.span()));
            }
            case DslToken.OpenBrace openBrace -> {
                var map = Lists.<Pair<Expression, Expression>>newArrayList();
                while(!(peek() instanceof DslToken.CloseBrace)) {
                    var key = parseValue();
                    expect(DslToken.EqualSymbol.class);
                    var value = parseValue();
                    if(!(peek() instanceof DslToken.CloseBrace)) {
                        expect(DslToken.Comma.class);
                    }
                    map.add(Pair.of(key, value));
                }
                var closeBrace = expect(DslToken.CloseBrace.class);
                yield new InlineDictExpression(map, SpanData.merge(openBrace.span(), closeBrace.span()));
            }
            case DslToken.OpenParen(SpanData span) -> {
                var value = parseValue();
                expect(DslToken.CloseParen.class);
                yield value;
            }
            default -> throw new ParsingException(tok + " is not a valid value, expected one of: Number, String, Text, Identifier", tok.span());
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
            throw new ParsingException("Expected " + clazz.getSimpleName() + ", but instead found " + token.getClass().getSimpleName(), token.span());
        }
    }
}
