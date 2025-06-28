package dev.akarah.cdata.registry.text;

import dev.akarah.cdata.registry.text.arguments.FunctionCall;
import dev.akarah.cdata.registry.text.arguments.NumberArgument;
import dev.akarah.cdata.registry.text.arguments.StringArgument;

import java.util.List;

public interface FunctionArgument {
    default double asNumber() {
        if (this instanceof NumberArgument(double number)) {
            return number;
        } else {
            return 0.0;
        }
    }

    default String asString() {
        if (this instanceof StringArgument(String string)) {
            return string;
        } else {
            return "";
        }
    }

    default FunctionCall asFunctionCall() {
        if(this instanceof FunctionCall fn) {
            return fn;
        } else {
            return new FunctionCall("const", List.of(this));
        }
    }
}
