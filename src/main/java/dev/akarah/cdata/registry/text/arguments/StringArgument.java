package dev.akarah.cdata.registry.text.arguments;

import dev.akarah.cdata.registry.text.FunctionArgument;
import org.jetbrains.annotations.NotNull;

public record StringArgument(String value) implements FunctionArgument {
    @Override
    public @NotNull String toString() {
        return this.value;
    }
}
