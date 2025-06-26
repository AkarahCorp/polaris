package dev.akarah.cdata.registry.text;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.property.Properties;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.ExtRegistries;
import net.fabricmc.api.Environment;
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
        public FunctionArgument evaluate(ParseContext environment) {
            try {
                return switch (name) {
                    case "property" -> new FunctionArgument.StringArgument(environment.defaultProperties().get(
                            ExtBuiltInRegistries.PROPERTIES
                                    .get(ResourceLocation.parse(arguments.getFirst().asString()))
                                    .orElseThrow()
                                    .value()
                    ).orElseThrow().toString());
                    case "skip_if_missing_property", "property?" -> {
                        var properties = environment.defaultProperties();
                        for(var argument : arguments) {
                            var statName = ResourceLocation.parse(argument.asString());
                            if(properties.has(ExtBuiltInRegistries.PROPERTIES.getValue(statName))) {
                                yield new FunctionArgument.StringArgument("");
                            }
                        }
                        yield new FunctionCall("skip", List.of());
                    }
                    case "stat" -> new FunctionArgument.NumberArgument(
                            environment.defaultStatsObject().get(ResourceLocation.parse(arguments.getFirst().asString()))
                    );
                    case "skip_if_missing_stat", "stat?" -> {
                        var statsObject = environment.defaultStatsObject();
                        for(var argument : arguments) {
                            var statName = ResourceLocation.parse(argument.asString());
                            if(statsObject.has(statName)) {
                                yield new FunctionArgument.StringArgument("");
                            }
                        }
                        yield new FunctionCall("skip", List.of());
                    }
                    case "cdata" -> new FunctionArgument.StringArgument(
                        environment.defaultProperties().get(Properties.CUSTOM_DATA).orElseThrow()
                                .value()
                                .get(arguments.getFirst().asString())
                                .result().orElseThrow()
                                .asString("Error: A string was not provided!")
                    );
                    case "skip_if_missing_cdata", "cdata?" -> {
                        var test = environment.defaultProperties().get(Properties.CUSTOM_DATA)
                                .flatMap(x -> x.value().get(arguments.getFirst().asString()).result());
                        if(test.isPresent()) {
                            yield new FunctionArgument.StringArgument("");
                        } else {
                            yield new FunctionCall("skip", List.of());
                        }
                    }
                    case "const" -> arguments.getFirst();
                    case "skip" -> throw new SkipLineException("skip line");
                    default -> throw new RuntimeException("'" + name + "' is not a property");
                };
            } catch (RuntimeException e) {
                if(e instanceof SkipLineException skipLineException) {
                    throw skipLineException;
                }
                return new StringArgument("undefined: " + e + " , name=" + name + ", args=" + arguments.toString());
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
