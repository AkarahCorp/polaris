package dev.akarah.cdata.script.dsl;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DataResult;

import java.util.ArrayList;
import java.util.List;

public class DslTokenizer {
    StringReader stringReader;

    public static DataResult<List<DslToken>> tokenize(String input) {
        var tk = new DslTokenizer();
        tk.stringReader = new StringReader(input);

        var list = tk.tokenizeLoop();
        list.ifSuccess(inner -> {
            inner.add(new DslToken.EOF());
            inner.add(new DslToken.EOF());
            inner.add(new DslToken.EOF());
            inner.add(new DslToken.EOF());
            inner.add(new DslToken.EOF());
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
            switch (stringReader.peek()) {
                case '"' -> {
                    return DataResult.success(new DslToken.StringExpr(this.stringReader.readQuotedString()));
                }
                case '$' -> {
                    this.stringReader.expect('$');
                    return DataResult.success(new DslToken.TextExpr(this.stringReader.readQuotedString()));
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    return DataResult.success(new DslToken.NumberExpr(this.stringReader.readDouble()));
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                     'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                     'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                     'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                     '_', '.' -> {
                    var string = this.stringReader.readUnquotedString();
                    return DataResult.success(switch (string) {
                        case "if" -> new DslToken.IfKeyword();
                        case "else" -> new DslToken.ElseKeyword();
                        case "local" -> new DslToken.LocalKeyword();
                        case "repeat" -> new DslToken.RepeatKeyword();
                        default -> new DslToken.Identifier(string);
                    });
                }
                case ';' -> {
                    stringReader.expect(';');
                    return DataResult.success(new DslToken.Semicolon());
                }
                case '(' -> {
                    stringReader.expect('(');
                    return DataResult.success(new DslToken.OpenParen());
                }
                case ')' -> {
                    stringReader.expect(')');
                    return DataResult.success(new DslToken.CloseParen());
                }
                case '{' -> {
                    stringReader.expect('{');
                    return DataResult.success(new DslToken.OpenBrace());
                }
                case '}' -> {
                    stringReader.expect('}');
                    return DataResult.success(new DslToken.CloseBrace());
                }
                case ',' -> {
                    stringReader.expect(',');
                    return DataResult.success(new DslToken.Comma());
                }
                default -> throw new SimpleCommandExceptionType(() -> "Invalid character type: '" + stringReader.peek() + "'")
                        .createWithContext(this.stringReader);
            }
        } catch (CommandSyntaxException exception) {
            return DataResult.error(exception::getMessage);
        } catch (StringIndexOutOfBoundsException exception) {
            return DataResult.success(new DslToken.EOF());
        }
    }
}
