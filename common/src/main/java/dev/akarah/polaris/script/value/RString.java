package dev.akarah.polaris.script.value;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;

public class RString extends RuntimeValue {
    private final String inner;


    public static String typeName() {
        return "string";
    }

    private RString(String inner) {
        this.inner = inner;
    }

    public static RString of(String value) {
        return new RString(value);
    }

    @Override
    public String javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return new RString(this.inner);
    }

    @MethodTypeHint(signature = "(this: string, target: string, sequence: string) -> string", documentation = "Replaces a substring with another.")
    public static RString replace(RString $this, RString target, RString sequence) {
        return RString.of($this.javaValue().replace(target.javaValue(), sequence.javaValue()));
    }

    @MethodTypeHint(signature = "(this: string) -> string", documentation = "Removes starting and ending whitespace.")
    public static RString trim(RString $this) {
        return RString.of($this.javaValue().trim());
    }

    @MethodTypeHint(signature = "(this: string) -> nullable[number]", documentation = "Attempts to parse a string into a number.")
    public static RNullable parse_number(RString $this) {
        try {
            return RNullable.of(RNumber.of(Double.parseDouble($this.javaValue())));
        } catch (Exception e) {
            return RNullable.empty();
        }
    }

    @MethodTypeHint(signature = "(this: string, regex: string) -> list[string]", documentation = "Splits a string into a list, delimited based on the regex.")
    public static RList split(RString $this, RString regex) {
        var list = RList.create();
        for(var entry : $this.javaValue().split(regex.javaValue())) {
            RList.add(list, RString.of(entry));
        }
        return list;
    }

    @MethodTypeHint(signature = "(this: string, content: string) -> boolean", documentation = "Checks if a string starts with a substring.")
    public static RBoolean starts_with(RString $this, RString content) {
        return RBoolean.of($this.inner.startsWith(content.javaValue()));
    }

    @MethodTypeHint(signature = "(this: string, content: string) -> boolean", documentation = "Checks if a string contains a substring.")
    public static RBoolean contains(RString $this, RString content) {
        return RBoolean.of($this.inner.contains(content.javaValue()));
    }

    @MethodTypeHint(signature = "(this: string, start: number, end: number) -> string", documentation = "Checks if a string contains a substring.")
    public static RString substring(RString $this, RNumber start, RNumber end) {
        assert end.doubleValue() >= start.doubleValue();
        assert start.doubleValue() >= 0;
        assert end.doubleValue() >= 0;
        return RString.of($this.inner.substring(start.intValue(), end.intValue()));
    }

    @MethodTypeHint(signature = "(this: string, concat: any) -> string", documentation = "Returns a new string, concatenating 2 strings.")
    public static RString add(RString $this, RuntimeValue convert) {
        return RString.of($this.javaValue() + convert);
    }

    @MethodTypeHint(signature = "(this: string) -> number", documentation = "Returns the length of the string.")
    public static RNumber size(RString $this) {
        return RNumber.of($this.javaValue().length());
    }
}
