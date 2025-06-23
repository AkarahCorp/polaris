package dev.akarah.cdata.registry.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.akarah.cdata.property.PropertyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    StringReader string;
    List<FunctionArgument.FunctionCall> interpolations = new ArrayList<>();
    StringBuilder outputString = new StringBuilder();

    // syntax: %function(arg1,arg2)

    public static ParsedText parseTextLine(String textLine) {
        var p = new Parser();
        p.string = new StringReader(textLine);
        try {
            p.parseInterpolations();
        } catch (CommandSyntaxException e) {
            return new ParsedText("<red>syntax error: " + e, List.of());
        }
        return p.output();
    }

    public void parseInterpolations() throws CommandSyntaxException {
        int interpolations = 0;
        while (string.canRead()) {
            var nextChar = string.read();
            if(nextChar == '\\') {
                var following = string.read();
                outputString.append(following);
            } else if(nextChar == '%') {
                outputString.append("<interpolate:")
                        .append(interpolations)
                        .append(">");
                var function = readFunction();
                this.interpolations.add(function);
                interpolations += 1;
            } else {
                outputString.append(nextChar);
            }
        }
    }

    public FunctionArgument.FunctionCall readFunction() throws CommandSyntaxException {
        var name = string.readUnquotedString();
        var args = new ArrayList<FunctionArgument>();
        try {
            if(string.peek() == '(') {
                string.expect('(');
                while(true) {
                    string.skipWhitespace();
                    if(string.peek() == ')') {
                        string.expect(')');
                        return new FunctionArgument.FunctionCall(name, args);
                    }
                    if(string.peek() >= '0' && string.peek() <= '9'
                            || string.peek() == '-') {
                        args.add(new FunctionArgument.NumberArgument(string.readDouble()));
                    } else if(string.peek() == '%') {
                        args.add(readFunction());
                    } else if(string.peek() == '"') {
                        args.add(new FunctionArgument.StringArgument(string.readStringUntil('"')));
                        string.skip();
                    } else {
                        args.add(new FunctionArgument.StringArgument(string.readUnquotedString()));
                    }
                    if(string.peek() == ',') {
                        string.expect(',');
                    }
                }
            }
        } catch (StringIndexOutOfBoundsException ignored) {

        }
        return new FunctionArgument.FunctionCall(name, List.of());
    }

    public ParsedText output() {
        return new ParsedText(this.outputString.toString(), this.interpolations);
    }
}
