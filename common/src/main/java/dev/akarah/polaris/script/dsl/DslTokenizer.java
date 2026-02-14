package dev.akarah.polaris.script.dsl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import dev.akarah.polaris.script.exception.ParsingException;
import dev.akarah.polaris.script.exception.SpanData;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DslTokenizer {
    Identifier fileName;
    StringReader stringReader;

    public static DataResult<List<DslToken>> tokenize(Identifier fileName, String input) {
        var tk = new DslTokenizer();
        tk.fileName = fileName;
        tk.stringReader = new StringReader(input);

        var list = tk.tokenizeLoop();
        list.ifSuccess(inner -> {
            inner.add(new DslToken.EOF(tk.createSpan()));
            inner.add(new DslToken.EOF(tk.createSpan()));
            inner.add(new DslToken.EOF(tk.createSpan()));
            inner.add(new DslToken.EOF(tk.createSpan()));
            inner.add(new DslToken.EOF(tk.createSpan()));
        });
        return list;
    }

    public DataResult<List<DslToken>> tokenizeLoop() {
        var list = new ArrayList<DslToken>();
        while(true) {
            stringReader.skipWhitespace();
            DataResult<DslToken> token = tokenizeOnce();
            if(token.isError()) {
                return DataResult.error(() -> token.error().orElseThrow().message(), list);
            }
            if(token.getOrThrow() instanceof DslToken.EOF) {
                return DataResult.success(list);
            }
            list.add(token.getOrThrow());
        }
    }

    public String readQuotedString() throws CommandSyntaxException {
        if (!this.stringReader.canRead()) {
            return "";
        }
        final char next = this.stringReader.peek();
        if (!StringReader.isQuotedStringStart(next)) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedStartOfQuote().createWithContext(this.stringReader);
        }
        this.stringReader.skip();
        return readStringUntil(next);
    }

    public String readStringUntil(char terminator) throws CommandSyntaxException {
        final StringBuilder result = new StringBuilder();
        boolean escaped = false;
        while (this.stringReader.canRead()) {
            final char c = this.stringReader.read();
            if (escaped) {
                if (c == terminator || c == '\\') {
                    result.append(c);
                    escaped = false;
                } else if(c == 'n') {
                    result.append('\n');
                    escaped = false;
                } else {
                    this.stringReader.setCursor(this.stringReader.getCursor() - 1);
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(this.stringReader, String.valueOf(c));
                }
            } else if (c == '\\') {
                escaped = true;
            } else if (c == terminator) {
                return result.toString();
            } else {
                result.append(c);
            }
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(this.stringReader);
    }

    public DataResult<DslToken> tokenizeOnce() {
        try {
            stringReader.skipWhitespace();

            while(stringReader.peek() == '#') {
                while(stringReader.peek() != '\n') {
                    stringReader.skip();
                }
                stringReader.skipWhitespace();
            }
            stringReader.skipWhitespace();

            var start = this.stringReader.getCursor();
            return switch (stringReader.peek()) {
                case '"' ->
                        DataResult.success(new DslToken.StringExpr(readQuotedString(), this.createSpan(start)));
                case '$' -> {
                    this.stringReader.expect('$');

                    if(stringReader.peek() == '"') {
                        yield DataResult.success(new DslToken.TextExpr(readQuotedString(), this.createSpan(start)));
                    } else {
                        var namespace = this.readIdentifier();
                        this.stringReader.expect(':');
                        var path = this.readPath();
                        yield DataResult.success(new DslToken.NamespacedIdentifierExpr(namespace, path, this.createSpan(start)));
                    }
                }
                case '@' -> {
                    this.stringReader.expect('@');
                    var annotation = this.readIdentifier();
                    yield DataResult.success(new DslToken.Annotation(annotation, this.createSpan(start)));
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                        DataResult.success(new DslToken.NumberExpr(this.stringReader.readDouble(), this.createSpan(start)));
                case '-' -> {
                    stringReader.expect('-');
                    if(stringReader.peek() == '>') {
                        stringReader.expect('>');
                        yield DataResult.success(new DslToken.ArrowSymbol(this.createSpan(start)));
                    }
                    yield DataResult.success(new DslToken.MinusSymbol(this.createSpan(start)));
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                     'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                     'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                     'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                     '_' -> {
                    var string = this.readIdentifier();
                    yield DataResult.success(switch (string) {
                        case "if" -> new DslToken.IfKeyword(this.createSpan(start));
                        case "unless" -> new DslToken.UnlessKeyword(this.createSpan(start));
                        case "else" -> new DslToken.ElseKeyword(this.createSpan(start));
                        case "local" -> new DslToken.LocalKeyword(this.createSpan(start));
                        case "repeat" -> new DslToken.RepeatKeyword(this.createSpan(start));
                        case "function" -> new DslToken.FunctionKeyword(this.createSpan(start));
                        case "ref" -> new DslToken.RefKeyword(this.createSpan(start));
                        case "for" -> new DslToken.ForKeyword(this.createSpan(start));
                        case "in" -> new DslToken.InKeyword(this.createSpan(start));
                        case "break" -> new DslToken.BreakKeyword(this.createSpan(start));
                        case "continue" -> new DslToken.ContinueKeyword(this.createSpan(start));
                        case "return" -> new DslToken.ReturnKeyword(this.createSpan(start));
                        case "struct" -> new DslToken.StructKeyword(this.createSpan(start));
                        case "new" -> new DslToken.NewKeyword(this.createSpan(start));
                        case "event" -> new DslToken.EventKeyword(this.createSpan(start));
                        case "as" -> new DslToken.AsKeyword(this.createSpan(start));
                        case "switch" -> new DslToken.SwitchKeyword(this.createSpan(start));
                        case "case" -> new DslToken.CaseKeyword(this.createSpan(start));
                        case "where" -> new DslToken.WhereKeyword(this.createSpan(start));
                        case "or" -> new DslToken.LogicalOr(this.createSpan(start));
                        case "and" -> new DslToken.LogicalAnd(this.createSpan(start));
                        case "not" -> new DslToken.LogicalNot(this.createSpan(start));
                        case "with", "while", "until", "is" ->
                                throw new ParsingException("The keyword '" + string + "' is reserved", this.createSpan(start));
                        case "foreach" ->
                                throw new ParsingException("The keyword '" + string + "' is deprecated", this.createSpan(start));
                        default -> new DslToken.Identifier(string, this.createSpan(start));
                    });
                }
                case ';' -> token(';', () -> new DslToken.Semicolon(this.createSpan(start)));
                case ':' -> token(':', () -> new DslToken.Colon(this.createSpan(start)));
                case '=' -> tokenWithEquals('=', () -> new DslToken.EqualSymbol(this.createSpan(start)), () -> new DslToken.DoubleEqualSymbol(this.createSpan(start)));
                case '(' -> token('(', () -> new DslToken.OpenParen(this.createSpan(start)));
                case ')' -> token(')', () -> new DslToken.CloseParen(this.createSpan(start)));
                case '{' -> token('{', () -> new DslToken.OpenBrace(this.createSpan(start)));
                case '}' -> token('}', () -> new DslToken.CloseBrace(this.createSpan(start)));
                case '[' -> token('[', () -> new DslToken.OpenBracket(this.createSpan(start)));
                case ']' -> token(']', () -> new DslToken.CloseBracket(this.createSpan(start)));
                case ',' -> token(',', () -> new DslToken.Comma(this.createSpan(start)));
                case '+' -> token('+', () -> new DslToken.PlusSymbol(this.createSpan(start)));
                case '*' -> token('*', () -> new DslToken.StarSymbol(this.createSpan(start)));
                case '/' -> token('/', () -> new DslToken.SlashSymbol(this.createSpan(start)));
                case '>' -> tokenWithEquals('>', () -> new DslToken.GreaterThanSymbol(this.createSpan(start)), () -> new DslToken.GreaterThanOrEqualSymbol(this.createSpan(start)));
                case '<' -> tokenWithEquals('<', () -> new DslToken.LessThanSymbol(this.createSpan(start)), () -> new DslToken.LessThanOrEqualSymbol(this.createSpan(start)));
                case '?' -> token('?', () -> new DslToken.QuestionMark(this.createSpan(start)));
                case '%' -> token('%', () -> new DslToken.Percent(this.createSpan(start)));
                case '!' -> {
                    stringReader.expect('!');
                    if(stringReader.peek() == '=') {
                        stringReader.expect('=');
                        yield DataResult.success(new DslToken.NotEqualSymbol(this.createSpan(start)));
                    } else {
                        throw new ParsingException("Exclamation marks must be immediately succeeded with an equals sign", this.createSpan(start));
                    }
                }
                default -> throw new ParsingException("Invalid character type: '" + stringReader.peek() + "'", this.createSpan());
            };
        } catch (CommandSyntaxException exception) {
            return DataResult.error(exception::getMessage);
        } catch (StringIndexOutOfBoundsException exception) {
            return DataResult.success(new DslToken.EOF(this.createSpan()));
        }
    }

    public DataResult<DslToken> token(char symbol, Supplier<DslToken> token) throws CommandSyntaxException {
        stringReader.expect(symbol);
        return DataResult.success(token.get());
    }

    public SpanData createSpan() {
        return new SpanData(this.stringReader.getCursor(), this.stringReader.getCursor(), this.stringReader.getString(), fileName);
    }

    public SpanData createSpan(int start) {
        return new SpanData(start, this.stringReader.getCursor(), this.stringReader.getString(), fileName);
    }

    public SpanData createSpan(int start, int end) {
        return new SpanData(start, end, this.stringReader.getString(), fileName);
    }

    public static boolean isAllowedInIdentifier(final char c) {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '.';
    }

    public static boolean isAllowedInPath(final char c) {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '.' || c == '/';
    }

    public String readIdentifier() {
        final int start = this.stringReader.getCursor();
        while (this.stringReader.canRead() && isAllowedInIdentifier(this.stringReader.peek())) {
            this.stringReader.skip();
        }
        return this.stringReader.getString().substring(start, this.stringReader.getCursor());
    }

    public String readPath() {
        final int start = this.stringReader.getCursor();
        while (this.stringReader.canRead() && isAllowedInPath(this.stringReader.peek())) {
            this.stringReader.skip();
        }
        return this.stringReader.getString().substring(start, this.stringReader.getCursor());
    }

    public DataResult<DslToken> tokenWithEquals(char symbol, Supplier<DslToken> token, Supplier<DslToken> tokenWithEquals) throws CommandSyntaxException {
        stringReader.expect(symbol);
        if(stringReader.peek() == '=') {
            stringReader.expect('=');
            return DataResult.success(tokenWithEquals.get());
        } else {
            return DataResult.success(token.get());
        }
    }
}
