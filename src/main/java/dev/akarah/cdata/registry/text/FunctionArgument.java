package dev.akarah.cdata.registry.text;

import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface FunctionArgument {
    record NumberArgument(double number) implements FunctionArgument {
        @Override
        public @NotNull String toString() {
            return Double.toString(this.number);
        }
    }

    record StringArgument(String value) implements FunctionArgument {
        @Override
        public @NotNull String toString() {
            return this.value;
        }
    }

    record FunctionCall(
            String name,
            List<FunctionArgument> arguments
    ) implements FunctionArgument {
        public FunctionArgument evaluate(Parser.Environment environment) {
            try {
                return switch (name) {
                    case "property" -> new FunctionArgument.StringArgument(environment.defaultProperties().get(
                            ExtBuiltInRegistries.PROPERTIES
                                    .get(ResourceLocation.parse(arguments.getFirst().asString()))
                                    .orElseThrow()
                                    .value()
                    ).orElseThrow().toString());
                    case "const" -> arguments.getFirst();
                    case "skip" -> throw new SkipLineException("skip line");
                    default -> throw new RuntimeException();
                };
            } catch (RuntimeException e) {
                if(e instanceof SkipLineException skipLineException) {
                    throw skipLineException;
                }
                return new StringArgument("undefined");
            }
        }
    }

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
