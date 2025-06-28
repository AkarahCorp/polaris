package dev.akarah.cdata.registry.text;

import dev.akarah.cdata.registry.citem.CustomItem;
import dev.akarah.cdata.registry.stat.StatsObject;

import java.util.Optional;

public record ParseContext(
        Optional<CustomItem> itemProperties,
        Optional<StatsObject> stats
) {
    public static ParseContext empty() {
        return new ParseContext(
                Optional.empty(),
                Optional.empty()
        );
    }

    public static ParseContext item(CustomItem customItem) {
        return new ParseContext(
                Optional.of(customItem),
                Optional.of(customItem.stats().orElse(StatsObject.EMPTY))
        );
    }
}
