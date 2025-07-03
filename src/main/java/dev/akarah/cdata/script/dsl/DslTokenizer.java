package dev.akarah.cdata.script.dsl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DataResult;
import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class DslTokenizer {
    ResourceLocation fileName;
    StringReader stringReader;

    public static DataResult<List<DslToken>> tokenize(ResourceLocation fileName, String input) {
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

    public DataResult<DslToken> tokenizeOnce() {
        try {
            stringReader.skipWhitespace();
            var start = this.stringReader.getCursor();
            switch (stringReader.peek()) {
                case '"' -> {
                    return DataResult.success(new DslToken.StringExpr(this.stringReader.readQuotedString(), this.createSpan(start)));
                }
                case '$' -> {
                    this.stringReader.expect('$');
                    return DataResult.success(new DslToken.TextExpr(this.stringReader.readQuotedString(), this.createSpan(start)));
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    return DataResult.success(new DslToken.NumberExpr(this.stringReader.readDouble(), this.createSpan(start)));
                }
                case '-' -> {
                    stringReader.expect('-');
                    if(stringReader.peek() == '>') {
                        stringReader.expect('>');
                        return DataResult.success(new DslToken.ArrowSymbol(this.createSpan(start)));
                    }
                    return DataResult.success(new DslToken.MinusSymbol(this.createSpan(start)));
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                     'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                     'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                     'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                     '_', '.' -> {
                    var string = this.stringReader.readUnquotedString();
                    return DataResult.success(switch (string) {
                        case "if" -> new DslToken.IfKeyword(this.createSpan(start));
                        case "else" -> new DslToken.ElseKeyword(this.createSpan(start));
                        case "local" -> new DslToken.LocalKeyword(this.createSpan(start));
                        case "repeat" -> new DslToken.RepeatKeyword(this.createSpan(start));
                        case "schema" -> new DslToken.SchemaKeyword(this.createSpan(start));
                        default -> new DslToken.Identifier(string, this.createSpan(start));
                    });
                }
                case ';' -> {
                    stringReader.expect(';');
                    return DataResult.success(new DslToken.Semicolon(this.createSpan(start)));
                }
                case '=' -> {
                    stringReader.expect('=');
                    return DataResult.success(new DslToken.EqualSymbol(this.createSpan(start)));
                }
                case '(' -> {
                    stringReader.expect('(');
                    return DataResult.success(new DslToken.OpenParen(this.createSpan(start)));
                }
                case ')' -> {
                    stringReader.expect(')');
                    return DataResult.success(new DslToken.CloseParen(this.createSpan(start)));
                }
                case '{' -> {
                    stringReader.expect('{');
                    return DataResult.success(new DslToken.OpenBrace(this.createSpan(start)));
                }
                case '}' -> {
                    stringReader.expect('}');
                    return DataResult.success(new DslToken.CloseBrace(this.createSpan(start)));
                }
                case ',' -> {
                    stringReader.expect(',');
                    return DataResult.success(new DslToken.Comma(this.createSpan(start)));
                }
                default -> throw new ParsingException("Invalid character type: '" + stringReader.peek() + "'", this.createSpan());
            }
        } catch (CommandSyntaxException exception) {
            return DataResult.error(exception::getMessage);
        } catch (StringIndexOutOfBoundsException exception) {
            return DataResult.success(new DslToken.EOF(this.createSpan()));
        }
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
}
