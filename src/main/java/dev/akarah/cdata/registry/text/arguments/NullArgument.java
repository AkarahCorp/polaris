package dev.akarah.cdata.registry.text.arguments;

import dev.akarah.cdata.registry.text.FunctionArgument;
import org.jetbrains.annotations.NotNull;

public record NullArgument() implements FunctionArgument {
    @Override
    public @NotNull String toString() {
        return "null";
    }
}
