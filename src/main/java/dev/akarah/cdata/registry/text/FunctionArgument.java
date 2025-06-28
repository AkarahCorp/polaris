package dev.akarah.cdata.registry.text;

import dev.akarah.cdata.registry.citem.CustomItem;
import dev.akarah.cdata.registry.stat.StatsObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

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

    record NullArgument() implements FunctionArgument {
        @Override
        public @NotNull String toString() {
            return "null";
        }
    }

    record FunctionCall(
            String name,
            List<FunctionArgument> arguments
    ) implements FunctionArgument {
        public FunctionArgument resolveProperty(ParseContext environment, String path) {
            var paths = path.split("\\.");
            return switch (paths[0]) {
                case "entity" -> switch (paths[1]) {
                    default -> new StringArgument("");
                };
                case "item" -> switch (paths[1]) {
                    case "name" -> new StringArgument(
                            environment.itemProperties()
                                    .flatMap(CustomItem::name)
                                    .orElseGet(() -> new ParsedText("", List.of()))
                                    .string()
                    );
                    default -> new StringArgument("Invalid property: " + paths[1]);
                };
                default -> new NullArgument();
            };
        }

        public StatsObject getStatsFromCategory(ParseContext environment, String category) {
            return switch (category) {
                case "item" -> environment.itemStats().orElse(StatsObject.EMPTY);
                case "entity" -> environment.entityStats().orElse(StatsObject.EMPTY);
                default -> StatsObject.EMPTY;
            };
        }

        public double getStat(ParseContext environment, String path) {
            var paths = path.split("\\.");
            var category = paths[0];
            var statName = paths[1];
            return getStatsFromCategory(environment, category).get(ResourceLocation.parse(statName));
        }

        public FunctionArgument evaluate(ParseContext environment) {
            try {
                return switch (name) {
                    case "property" -> resolveProperty(environment, arguments.getFirst().asString());
                    case "skip_if_missing_property", "property?" -> {
                        for(var argument : arguments) {
                            var resolved = resolveProperty(environment, argument.asString());
                            if(resolved instanceof NullArgument) {
                                yield resolved;
                            }
                        }
                        yield new FunctionCall("skip", List.of());
                    }
                    case "stat" -> new NumberArgument(getStat(environment, arguments.getFirst().asString()));
                    case "skip_if_missing_stat", "stat?" -> {
                        for(var argument : arguments) {
                            var stat = getStat(environment, argument.asString());
                            if(stat != 0.0) {
                                yield new FunctionArgument.StringArgument("");
                            }
                        }
                        yield new FunctionCall("skip", List.of());
                    }
                    case "cdata" -> new FunctionArgument.StringArgument(
                        environment.itemProperties().flatMap(CustomItem::customData).orElse(CustomData.EMPTY)
                                .copyTag()
                                .getStringOr(arguments.getFirst().asString(), "A string was not provided!")
                    );
                    case "skip_if_missing_cdata", "cdata?" -> {
                        var tag = environment.itemProperties().flatMap(CustomItem::customData).orElse(CustomData.EMPTY)
                                .copyTag();
                        for(var argument : arguments) {
                            var key = tag.get(argument.asString());
                            if(key != null) {
                                yield new FunctionArgument.StringArgument("");
                            }
                        }
                        yield new FunctionCall("skip", List.of());
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
