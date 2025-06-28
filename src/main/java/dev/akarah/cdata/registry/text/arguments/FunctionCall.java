package dev.akarah.cdata.registry.text.arguments;

import dev.akarah.cdata.registry.citem.CustomItem;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.registry.text.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public record FunctionCall(
        String name,
        List<FunctionArgument> arguments
) implements FunctionArgument {
    public FunctionArgument resolveProperty(ParseContext environment, String path) {
        NullList<String> paths = new NullList<>(List.of(path.split("\\.")));
        return switch (paths.getFirst()) {
            case "item" -> {
                paths.removeFirst();
                yield resolveItemProperty(environment, paths);
            }
            default -> {
                if (environment.itemProperties().isPresent()) {
                    yield resolveItemProperty(environment, paths);
                }
                yield new NullArgument();
            }
        };
    }

    public FunctionArgument resolveItemProperty(ParseContext environment, List<String> paths) {
        var propertyName = paths.removeFirst();
        System.out.println(propertyName);
        return switch (propertyName) {
            case "name" -> new StringArgument(
                    environment.itemProperties()
                            .flatMap(CustomItem::name)
                            .orElse("")
            );
            case "model" -> new StringArgument(
                    environment.itemProperties()
                            .map(CustomItem::model)
                            .map(Object::toString)
                            .orElse("name")
            );
            default -> new StringArgument("Invalid property: " + propertyName);
        };
    }

    public double getStat(ParseContext environment, String statName) {
        return environment.stats().orElse(StatsObject.EMPTY).get(ResourceLocation.parse(statName));
    }

    public FunctionArgument evaluate(ParseContext environment) {
        try {
            return switch (name) {
                case "property" -> resolveProperty(environment, arguments.getFirst().asString());
                case "stat" -> new NumberArgument(getStat(environment, arguments.getFirst().asString()));
                case "skip_if_missing_stat", "stat?" -> {
                    for (var argument : arguments) {
                        var stat = getStat(environment, argument.asString());
                        if (stat != 0.0) {
                            yield new StringArgument("");
                        }
                    }
                    yield new dev.akarah.cdata.registry.text.arguments.FunctionCall("skip", List.of());
                }
                case "cdata" -> new StringArgument(
                        environment.itemProperties().flatMap(CustomItem::customData).orElse(CustomData.EMPTY)
                                .copyTag()
                                .getStringOr(arguments.getFirst().asString(), "A string was not provided!")
                );
                case "skip_if_missing_cdata", "cdata?" -> {
                    var tag = environment.itemProperties().flatMap(CustomItem::customData).orElse(CustomData.EMPTY)
                            .copyTag();
                    for (var argument : arguments) {
                        var key = tag.get(argument.asString());
                        if (key != null) {
                            yield new StringArgument("");
                        }
                    }
                    yield new dev.akarah.cdata.registry.text.arguments.FunctionCall("skip", List.of());
                }
                case "const" -> arguments.getFirst();
                case "skip" -> throw new SkipLineException("skip line");
                default -> throw new RuntimeException("'" + name + "' is not a property");
            };
        } catch (RuntimeException e) {
            if (e instanceof SkipLineException skipLineException) {
                throw skipLineException;
            }
            return new StringArgument("undefined: " + e + " , name=" + name + ", args=" + arguments.toString());
        }
    }
}
