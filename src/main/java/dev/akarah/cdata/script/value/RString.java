package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

public class RString extends RuntimeValue {
    private final String inner;

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

    @MethodTypeHint(signature = "(this: string, target: string, sequence: string) -> string", documentation = "Replaces a substring with another.")
    public static RString replace(RString $this, RString target, RString sequence) {
        return RString.of($this.javaValue().replace(target.javaValue(), sequence.javaValue()));
    }

    @MethodTypeHint(signature = "(this: string) -> string", documentation = "Removes starting and ending whitespace.")
    public static RString trim(RString $this) {
        return RString.of($this.javaValue().trim());
    }

    @MethodTypeHint(signature = "(this: string) -> nullable[number]", documentation = "Attempts to parsing.")
    public static RNullable parse_number(RString $this) {
        try {
            return RNullable.of(RNumber.of(Double.parseDouble($this.javaValue())));
        } catch (Exception e) {
            return RNullable.empty();
        }
    }

    @MethodTypeHint(signature = "(this: string, regex: string) -> list[string]", documentation = "Splits a string into a list, each entry is delimited based on the regex.")
    public static RList split(RString $this, RString regex) {
        var list = RList.create();
        for(var entry : $this.javaValue().split(regex.javaValue())) {
            RList.add(list, RString.of(entry));
        }
        return list;
    }

    @MethodTypeHint(signature = "(base: string, concat: any) -> string", documentation = "Returns a new string, concatenating 2 strings.")
    public static RString add(RString base, RuntimeValue convert) {
        return RString.of(base.javaValue() + convert.toString());
    }
}
